package com.derongan.minecraft.deeperworld.listeners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.derongan.minecraft.deeperworld.*
import com.derongan.minecraft.deeperworld.config.DeeperConfig
import com.derongan.minecraft.deeperworld.datastructures.VehicleTree
import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.extensions.getLeashedEntities
import com.derongan.minecraft.deeperworld.extensions.getPassengersRecursive
import com.derongan.minecraft.deeperworld.extensions.getRootVehicle
import com.derongan.minecraft.deeperworld.extensions.teleportWithSpectatorsAsync
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.services.canMoveSections
import com.derongan.minecraft.deeperworld.world.section.*
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.info
import com.okkero.skedule.schedule
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.util.Vector

object MovementListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onPlayerMove(playerMoveEvent: PlayerMoveEvent) {
        val (player) = playerMoveEvent
        if (player.hasPermission(Permissions.CHANGE_SECTION_PERMISSION) && player.canMoveSections) {
            onPlayerMoveInternal(player, playerMoveEvent.from, playerMoveEvent.to ?: return)
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onVehicleMove(vehicleMoveEvent: VehicleMoveEvent) {
        val (vehicle) = vehicleMoveEvent
        val players = vehicle.getPassengersRecursive().filterIsInstance<Player>()

        players.firstOrNull { it.hasPermission(Permissions.CHANGE_SECTION_PERMISSION) && it.canMoveSections }?.let {
            onPlayerMoveInternal(it, vehicleMoveEvent.from, vehicleMoveEvent.to)
        }
    }

    private fun onPlayerMoveInternal(player: Player, from: Location, to: Location) {
        val current = WorldManager.getSectionFor(player.location) ?: let {
            //damage players outside of sections
            if (DeeperConfig.data.damageOutsideSections > 0.0
                && player.location.world !in DeeperConfig.data.damageExcludedWorlds
                && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)
                && player.location.world in (DeeperConfig.data.worlds)
            ) {
                player.damage(0.01) //give a damage effect
                player.health = (player.health - DeeperConfig.data.damageOutsideSections / 10)
                    .coerceIn(0.0, player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value) //ignores armor
                player.sendTitle(
                    "&cYou are not in a managed section".color(),
                    "&7You will take damage upon moving!".color(),
                    0, 20, 10
                )
            }
            return
        }

        val changeY = to.y - from.y
        if (changeY == 0.0) return

        val inSpectator = player.gameMode == GameMode.SPECTATOR

        fun tpIfAbleTo(
            key: SectionKey,
            tpFun: (Player, Location, Section, Section) -> Unit,
            boundaryCheck: (y: Double, shared: Int) -> Boolean,
            pushVelocity: Double
        ) {
            val toSection = key.section ?: return
            val overlap = current.overlapWith(toSection) ?: return
            val correspondingPos = to.getCorrespondingLocation(current, toSection) ?: return

            if (boundaryCheck(to.y, overlap)) {
                if (!toSection.region.contains(correspondingPos.blockX, correspondingPos.blockZ)
                    || !inSpectator && correspondingPos.block.type.isSolid
                )
                    player.velocity = player.velocity.setY(pushVelocity)
                else
                    tpFun(player, to, current, toSection)
            }
        }

        when {
            changeY > 0.0 -> tpIfAbleTo(
                current.aboveKey,
                MovementListener::ascend,
                { y, shared -> y > MinecraftConstants.WORLD_HEIGHT - .3 * shared },
                -0.4
            )
            changeY < 0.0 -> tpIfAbleTo(
                current.belowKey,
                MovementListener::descend,
                { y, shared -> y < .3 * shared },
                0.4
            )
        }
    }

    private fun descend(player: Player, to: Location, oldSection: Section, newSection: Section) {
        PlayerDescendEvent(player, oldSection, newSection).call {
            teleportBetweenSections(player, to, oldSection, newSection)
        }
    }

    private fun ascend(player: Player, to: Location, oldSection: Section, newSection: Section) {
        PlayerAscendEvent(player, oldSection, newSection).call {
            teleportBetweenSections(player, to, oldSection, newSection)
        }
    }

    private fun teleportBetweenSections(player: Player, to: Location, oldSection: Section, newSection: Section) {
        val newLoc = to.getCorrespondingLocation(oldSection, newSection) ?: return

        val oldLeashedEntities = player.getLeashedEntities()

        // Unleash all the leashed entities before teleporting them, to prevent leads from dropping.
        // The leashes are restored after teleportation.
        oldLeashedEntities.forEach {
            it.setLeashHolder(null)
        }

        val rootVehicle = player.getRootVehicle()
        if (rootVehicle != null) {
            newLoc.yaw = player.location.yaw
            newLoc.pitch = player.location.pitch

            // Prevent teleportation of other players in the vehicle-passenger structure.
            rootVehicle.getPassengersRecursive().filterIsInstance<Player>().forEach {
                if (it != player) {
                    it.vehicle?.removePassenger(it)
                }
            }

            val oldFallDistance = rootVehicle.fallDistance
            val oldVelocity = rootVehicle.velocity

            val vehicleTree = VehicleTree(rootVehicle)

            // Dismount every passenger in the vehicleTree in order to teleport them separately with a delay.
            // This avoids the bug of entities not rendering if they are teleported within 1 tick of the player.
            vehicleTree.root.applyAll {
                it.value.passengers.forEach { passenger ->
                    it.value.removePassenger(passenger)
                }
            }

            // Delay the teleportation by 1 tick after passenger removal to avoid occasional
            // "Removing ticking entity!" exceptions.
            deeperWorld.schedule {
                waitFor(1)

                player.teleportWithSpectatorsAsync(newLoc) {
                    protocolManager.addPacketListener(
                        SectionTeleportPacketAdapter(
                            player,
                            oldLeashedEntities,
                            oldFallDistance,
                            oldVelocity,
                            vehicleTree
                        )
                    )
                }

            }
        } else {
            val oldFallDistance = player.fallDistance
            val oldVelocity = player.velocity

            player.teleportWithSpectatorsAsync(newLoc) {
                player.fallDistance = oldFallDistance
                player.velocity = oldVelocity

                if (oldLeashedEntities.isNotEmpty()) {
                    protocolManager.addPacketListener(
                        SectionTeleportPacketAdapter(
                            player,
                            oldLeashedEntities,
                            oldFallDistance,
                            oldVelocity
                        )
                    )
                }
            }
        }
    }
}

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
) : PacketAdapter(deeperWorld, PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK) {
    override fun onPacketReceiving(event: PacketEvent) {
        if (event.player != player) return

        protocolManager.removePacketListener(this)

        deeperWorld.schedule {
            waitFor(1)

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

                waitFor(DeeperConfig.data.remountPacketDelay.inTicks)

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
