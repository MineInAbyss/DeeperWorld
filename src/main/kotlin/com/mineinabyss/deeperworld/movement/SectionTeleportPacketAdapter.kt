package com.mineinabyss.deeperworld.movement

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.config.deeperConfig
import com.mineinabyss.deeperworld.datastructures.VehicleTree
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.protocolManager
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
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
) : PacketAdapter(
    deeperWorld,
    PacketType.Play.Client.POSITION,
    PacketType.Play.Client.POSITION_LOOK
) {
    override fun onPacketReceiving(event: PacketEvent) {
        if (event.player != player) return

        protocolManager.removePacketListener(this)

        deeperWorld.launch {
            delay(1.ticks)

            oldLeashedEntities.toSet().forEach {
                if (it == player) return@forEach

                it.teleport(player)
                it.setLeashHolder(player)
            }

            if (vehicleTree != null) {
                vehicleTree.root.values().toSet().forEach {
                    if (it == player) return@forEach

                    it.teleport(player)
                }

                vehicleTree.root.applyAll { vehicleNode ->
                    vehicleNode.children.forEach {
                        vehicleNode.value.addPassenger(it.value)
                    }
                }

                vehicleTree.root.value.fallDistance = oldFallDistance
                vehicleTree.root.value.velocity = oldVelocity

                delay(deeperConfig.remountPacketDelay)

                player.vehicle?.let { vehicle ->
                    val playerVehicleID = vehicle.entityId
                    val passengerIDs = vehicle.passengers.map { it.entityId }.toIntArray()

                    // Resends a mount packet to clients to prevent potential visual glitches where the client thinks it's dismounted.
                    protocolManager.sendServerPacket(player, PacketContainer(PacketType.Play.Server.MOUNT).apply {
                        integers.write(0, playerVehicleID)
                        integerArrays.write(0, passengerIDs)
                    })
                }
            }
        }
    }
}
