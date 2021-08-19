package com.derongan.minecraft.deeperworld.movement

import com.derongan.minecraft.deeperworld.datastructures.VehicleTree
import com.derongan.minecraft.deeperworld.deeperWorld
import com.derongan.minecraft.deeperworld.extensions.getLeashedEntities
import com.derongan.minecraft.deeperworld.extensions.getPassengersRecursive
import com.derongan.minecraft.deeperworld.extensions.getRootVehicle
import com.derongan.minecraft.deeperworld.extensions.teleportWithSpectatorsAsync
import com.derongan.minecraft.deeperworld.protocolManager
import com.okkero.skedule.schedule
import org.bukkit.Location
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
            deeperWorld.schedule {
                waitFor(1)

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
}