package com.derongan.minecraft.deeperworld.synchronization

import com.derongan.minecraft.deeperworld.DeeperContext
import nl.rutgerkok.blocklocker.SearchMode
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.block.Sign
import org.bukkit.block.data.Waterlogged
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent

/**
 * Synchronizes the overlap between sections
 */
object SectionSyncListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun syncBlockBreak(blockBreakEvent: BlockBreakEvent) {
        val block = blockBreakEvent.block
        block.location.sync { original, corr ->
            val state = corr.state

            //if breaking from bottom container, drop items stored in top container here
            if (state is Container && original.location.y > corr.location.y) {
                val corrInv = state.inventory
                corrInv.toList().dropItems(original.location, false)
                corrInv.clear()
            }

            //sync any changes to BlockLocker's signs`
            if (DeeperContext.isBlockLockerLoaded && state is Sign && state.lines[0] == "[Private]") {
                blockLocker.protectionFinder.findProtection(corr, SearchMode.ALL).ifPresent {
                    it.signs.forEach { linkedSign -> linkedSign.location.block.type = Material.AIR }
                }
            }
            corr.type = Material.AIR
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun syncBlockPlace(blockPlaceEvent: BlockPlaceEvent) {
        blockPlaceEvent.block.sync()
    }

    //handles fertilized crops as well
    @EventHandler
    fun syncBlockGrow(blockEvent: BlockGrowEvent) {
        blockEvent.newState.location.sync()
    }

    //TODO this causes duplication glitches that need to be fixed first
    /*@EventHandler
    fun onBlockMultiPlaceEvent(blockEvent: BlockMultiPlaceEvent) {
        blockEvent.replacedBlockStates.copyBlocks()
    }*/

    @EventHandler
    fun syncWaterEmpty(event: PlayerBucketEmptyEvent) =
            event.block.sync { orig, corr ->
                val data = corr.blockData
                if (data is Waterlogged) {
                    data.isWaterlogged = true
                    corr.blockData = data
//                    corr.state.update(true, true) //TODO we need to send a block update somehow and this doesn't work
                } else
                    updateMaterial(Material.WATER)(orig, corr)
            }

    @EventHandler
    fun syncWaterFill(event: PlayerBucketFillEvent) =
            event.block.sync { orig, corr ->
                val data = corr.blockData
                if (data is Waterlogged) {
                    data.isWaterlogged = false
                    corr.blockData = data
                } else
                    updateMaterial(Material.AIR)(orig, corr)
            }

    /** Synchronize explosions */
    @EventHandler
    fun syncExplosions(explodeEvent: EntityExplodeEvent) {
        if (!explodeEvent.isCancelled)
            explodeEvent.blockList().forEach { explodedBlock ->
                explodedBlock.location.sync(updateMaterial(Material.AIR))
            }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun syncSignText(signChangeEvent: SignChangeEvent) {
        signChangeEvent.block.sync(signUpdater(signChangeEvent.lines))
    }
}