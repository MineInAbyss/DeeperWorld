package com.mineinabyss.deeperworld.movement

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.listeners.MovementListener
import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import com.mineinabyss.idofront.location.up
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class BedrockBlockingInvalidTeleportHandler(player: Player, from: Location, to: Location) : InvalidTeleportHandler(player, from, to) {

    constructor(player: Player, transition: SectionTransition) : this(player, transition.from, transition.to)
    override fun handleInvalidTeleport() {
        from.block.type = Material.BEDROCK

        val spawnedBedrock = from.block
        MovementListener.temporaryBedrock.add(spawnedBedrock)

        // Keep bedrock spawned if there are players within a 1.5 radius (regular jump height).
        // If no players are in this radius, destroy the bedrock.
        deeperWorld.plugin.launch {
            while (spawnedBedrock.location.up(1).getNearbyPlayers(1.5).isNotEmpty()) {
                delay(5.ticks)
            }
        }.invokeOnCompletion { //Will also run if plugin is unloaded
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
