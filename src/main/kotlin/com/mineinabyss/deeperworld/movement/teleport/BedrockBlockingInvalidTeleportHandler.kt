package com.mineinabyss.deeperworld.movement.teleport

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import com.mineinabyss.idofront.location.up
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity

class BedrockBlockingInvalidTeleportHandler : TeleportHandler, AutoCloseable {
    override val isValid: Boolean = false

    /** Blocks temporarily replaced with bedrock, mapped to their original data. */
    private val temporaryBedrock = mutableMapOf<Block, BlockData>()

    override fun handleTeleport(entity: Entity, transition: SectionTransition) {
        val bedrockBlock = transition.from.block
        if (bedrockBlock !in temporaryBedrock) {
            temporaryBedrock[bedrockBlock] = bedrockBlock.blockData
            bedrockBlock.type = Material.BEDROCK

            // Keep bedrock spawned if there are players within a 1.5 radius (regular jump height).
            // If no players are in this radius, restore the original block.
            deeperWorld.launch {
                while (bedrockBlock.location.up(1).getNearbyPlayers(1.5).isNotEmpty()) {
                    delay(5.ticks)
                }
            }.invokeOnCompletion { //Will also run if plugin is unloaded
                temporaryBedrock.remove(bedrockBlock)?.let {
                    if (bedrockBlock.type == Material.BEDROCK) bedrockBlock.blockData = it
                }
            }
        }

        val oldFallDistance = entity.fallDistance
        val oldVelocity = entity.velocity

        entity.teleport(bedrockBlock.location.up(1))

        entity.fallDistance = oldFallDistance
        entity.velocity = oldVelocity
    }

    override fun close() {
        temporaryBedrock.toMap().forEach { (block, data) ->
            if (block.type == Material.BEDROCK) block.blockData = data
        }
        temporaryBedrock.clear()
    }
}