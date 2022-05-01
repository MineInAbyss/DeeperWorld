package com.mineinabyss.deeperworld.movement

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.datastructures.VehicleTree
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.extensions.getPassengersRecursive
import com.mineinabyss.deeperworld.extensions.getRootVehicle
import com.mineinabyss.deeperworld.protocolManager
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class TransitionTeleportHandler(val player: Player, val from: Location, val to: Location) :
    TeleportHandler {

    override fun handleTeleport() {
        val oldLeashedEntities = player.getLeashedEntities()

        // Unleash all the leashed entities before teleporting them, to prevent leads from dropping.
        // The leashes are restored after teleportation.
        oldLeashedEntities.forEach {
            it.setLeashHolder(null)
        }

        val rootVehicle = player.getRootVehicle()
        if (rootVehicle != null) {
            to.yaw = player.location.yaw
            to.pitch = player.location.pitch

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
            deeperWorld.launch {
                delay(1.ticks)

                player.teleportWithSpectatorsAsync(to) {
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

            player.teleportWithSpectatorsAsync(to) {
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

    override fun isValidTeleport(): Boolean {
        return true
    }

    private fun Player.getLeashedEntities(): List<LivingEntity> {
        // Max leashed entity range is 10 blocks, therefore these parameter values
        return getNearbyEntities(20.0, 20.0, 20.0)
            .filterIsInstance<LivingEntity>()
            .filter { it.isLeashed && it.leashHolder == this }
    }

    private fun Player.teleportWithSpectatorsAsync(loc: Location, thenRun: (Boolean) -> Unit) {
        val nearbySpectators = getNearbyEntities(5.0, 5.0, 5.0)
            .filterIsInstance<Player>()
            .filter { it.spectatorTarget == this }

        nearbySpectators.forEach {
            it.spectatorTarget = null
        }

        teleportAsync(loc).thenAccept { success ->
            if (!success) return@thenAccept
            nearbySpectators.forEach {
                it.teleport(loc)
                it.spectatorTarget = this
            }
            thenRun(success)
        }
    }

}
