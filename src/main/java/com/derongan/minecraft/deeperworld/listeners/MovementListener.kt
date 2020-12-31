package com.derongan.minecraft.deeperworld.listeners

import com.derongan.minecraft.deeperworld.DeeperConfig
import com.derongan.minecraft.deeperworld.MinecraftConstants
import com.derongan.minecraft.deeperworld.Permissions
import com.derongan.minecraft.deeperworld.datastructures.VehicleTree
import com.derongan.minecraft.deeperworld.deeperWorld
import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.extensions.getLeashedEntities
import com.derongan.minecraft.deeperworld.extensions.getPassengersRecursive
import com.derongan.minecraft.deeperworld.extensions.getRootVehicle
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.services.canMoveSections
import com.derongan.minecraft.deeperworld.world.section.*
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.color
import com.okkero.skedule.schedule
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.vehicle.VehicleMoveEvent

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
            if (!DeeperConfig.data.damageExcludedWorlds.contains(player.location.world)
                    && DeeperConfig.data.damageOutsideSections > 0.0
                    && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)) {
                player.damage(0.01) //give a damage effect
                player.health = (player.health - DeeperConfig.data.damageOutsideSections / 10).coerceIn(0.0, player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value) //ignores armor
                player.sendTitle("&cYou are not in a managed section".color(), "&7You will take damage upon moving!".color(), 0, 20, 10)
            }
            return
        }

        val changeY = to.y - from.y
        if (changeY == 0.0) return

        val inSpectator = player.gameMode == GameMode.SPECTATOR

        fun tpIfAbleTo(key: SectionKey, tpFun: (Player, Location, Section, Section) -> Unit, boundaryCheck: (y: Double, shared: Int) -> Boolean, pushVelocity: Double) {
            val toSection = key.section ?: return
            val overlap = current.overlapWith(toSection) ?: return
            val correspondingPos = to.getCorrespondingLocation(current, toSection) ?: return

            if (boundaryCheck(to.y, overlap)) {
                if (!toSection.region.contains(correspondingPos.blockX, correspondingPos.blockZ)
                        || !inSpectator && correspondingPos.block.type.isSolid)
                    player.velocity = player.velocity.setY(pushVelocity)
                else
                    tpFun(player, to, current, toSection)
            }
        }

        when {
            changeY > 0.0 -> tpIfAbleTo(current.aboveKey, MovementListener::ascend, { y, shared -> y > MinecraftConstants.WORLD_HEIGHT - .3 * shared }, -0.4)
            changeY < 0.0 -> tpIfAbleTo(current.belowKey, MovementListener::descend, { y, shared -> y < .3 * shared }, 0.4)
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

            player.teleport(newLoc)

            deeperWorld.schedule {
                waitFor(DeeperConfig.data.entityTeleportDelay)

                vehicleTree.root.applyAll {
                    it.value.teleport(player)
                }

                vehicleTree.root.applyAll { vehicleNode ->
                    vehicleNode.children.forEach {
                        vehicleNode.value.addPassenger(it.value)
                    }
                }

                rootVehicle.fallDistance = oldFallDistance
                rootVehicle.velocity = oldVelocity

                oldLeashedEntities.forEach { leashEntity ->
                    leashEntity.teleport(player)
                    leashEntity.setLeashHolder(player)
                }
            }
        } else {
            val fallDistance = player.fallDistance
            val oldVelocity = player.velocity
            player.teleport(newLoc)
            player.fallDistance = fallDistance
            player.velocity = oldVelocity

            deeperWorld.schedule {
                waitFor(DeeperConfig.data.entityTeleportDelay)

                oldLeashedEntities.forEach { leashEntity ->
                    leashEntity.teleport(player)
                    leashEntity.setLeashHolder(player)
                }
            }
        }
    }
}
