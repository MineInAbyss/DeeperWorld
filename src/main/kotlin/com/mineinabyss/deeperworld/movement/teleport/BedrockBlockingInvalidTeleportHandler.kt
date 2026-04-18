package com.mineinabyss.deeperworld.movement.teleport

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import com.mineinabyss.idofront.location.up
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity

class BedrockBlockingInvalidTeleportHandler : TeleportHandler, AutoCloseable {
    override val isValid: Boolean = false
    private val temporaryBedrock = mutableListOf<Block>()

    override fun handleTeleport(entity: Entity, transition: SectionTransition) {
        val bedrockBlock = transition.from.block
        if (bedrockBlock.type == Material.AIR) temporaryBedrock += bedrockBlock
        transition.from.block.type = Material.BEDROCK

        // Keep bedrock spawned if there are players within a 1.5 radius (regular jump height).
        // If no players are in this radius, destroy the bedrock.
        deeperWorld.launch {
            while (bedrockBlock.location.up(1).getNearbyPlayers(1.5).isNotEmpty()) {
                delay(5.ticks)
            }
        }.invokeOnCompletion { //Will also run if plugin is unloaded
            if (bedrockBlock.type == Material.BEDROCK) bedrockBlock.type = Material.AIR
            temporaryBedrock -= bedrockBlock
        }

        val oldFallDistance = entity.fallDistance
        val oldVelocity = entity.velocity

        entity.teleport(bedrockBlock.location.up(1))

        entity.fallDistance = oldFallDistance
        entity.velocity = oldVelocity
    }

    override fun close() {
        temporaryBedrock.forEach { if (it.type == Material.BEDROCK) it.type = Material.AIR }
        temporaryBedrock.clear()
    }
}