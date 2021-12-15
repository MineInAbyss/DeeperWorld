package com.mineinabyss.deeperworld.movement

import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.listeners.MovementListener
import com.mineinabyss.idofront.location.up
import com.okkero.skedule.schedule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

class BedrockBlockingInvalidTeleportHandler(player: Player, from: Location, to: Location) :
    InvalidTeleportHandler(player, from, to) {
    override fun handleInvalidTeleport() {
        from.block.type = Material.BEDROCK

        val spawnedBedrock = from.block
        MovementListener.temporaryBedrock.add(spawnedBedrock)

        // Keep bedrock spawned if there are players within a 1.5 radius (regular jump height).
        // If no players are in this radius, destroy the bedrock.
        deeperWorld.schedule {
            this.repeating(1)
            while (spawnedBedrock.location.up(1).getNearbyPlayers(1.5).isNotEmpty()) {
                yield()
            }
            spawnedBedrock.type = Material.AIR
            MovementListener.temporaryBedrock.remove(spawnedBedrock)
        }

        val oldFallDistance = player.fallDistance
        val oldVelocity = player.velocity

        player.teleport(from.up(1))

        player.fallDistance = oldFallDistance
        player.velocity = oldVelocity
    }
}
