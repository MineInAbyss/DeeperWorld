package com.mineinabyss.deeperworld.movement

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.datastructures.VehicleTree
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.idofront.nms.PacketListener
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import net.kyori.adventure.key.Key
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

/**
 * This PacketAdapter serves to teleport entities with the player at the correct time
 * (after the client sends a POSITION or POSITION_LOOK packet). This circumvents the client side rendering bug
 * when entities are teleported with the player in the same tick.
 *
 * TODO: Remove listener if a player disconnects before sending a POSITION or POSITION_LOOK packet
 */
class SectionTeleportPacketAdapter(
    private val player: Player,
    private val oldLeashedEntities: List<LivingEntity>,
    private val oldFallDistance: Float,
    private val oldVelocity: Vector,
    private val vehicleTree: VehicleTree? = null
) {

    private val PACKET_KEY = Key.key("deeperworld", "section_teleport_handler_${player.name}")

    fun addPacketListener() {
        PacketListener.interceptClientbound(deeperWorld.plugin, PACKET_KEY.value()) { packet, player: Player? ->
            if (this.player.uniqueId != player?.uniqueId) return@interceptClientbound packet

            PacketListener.unregisterListener(PACKET_KEY)

            deeperWorld.plugin.launch {
                delay(1.ticks)

                oldLeashedEntities.asSequence().forEach {
                    if (it == player) return@forEach

                    it.teleport(player)
                    it.setLeashHolder(player)
                }

                if (vehicleTree != null) {
                    vehicleTree.root.values().asSequence().forEach {
                        if (it != player) it.teleport(player)
                    }

                    vehicleTree.root.applyAll { vehicleNode ->
                        vehicleNode.children.forEach {
                            vehicleNode.value.addPassenger(it.value)
                        }
                    }

                    vehicleTree.root.value.fallDistance = oldFallDistance
                    vehicleTree.root.value.velocity = oldVelocity

                    delay(deeperWorld.config.remountPacketDelay)

                    // Resends a mount packet to clients to prevent potential visual glitches where the client thinks it's dismounted.
                    player.vehicle?.toNMS()?.let { vehicle ->
                        player.toNMS().connection.send(ClientboundSetPassengersPacket(vehicle))
                    }
                }
            }

            return@interceptClientbound packet
        }
    }
}
