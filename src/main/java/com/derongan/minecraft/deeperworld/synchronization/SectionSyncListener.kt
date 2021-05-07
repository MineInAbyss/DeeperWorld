package com.derongan.minecraft.deeperworld.synchronization

import com.derongan.minecraft.deeperworld.DeeperContext
import com.mineinabyss.idofront.messaging.error
import nl.rutgerkok.blocklocker.SearchMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.ShulkerBox
import org.bukkit.block.Sign
import org.bukkit.block.data.Waterlogged
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent

private fun syncBlockLocker(corr: Block) {
    blockLocker.protectionFinder.findProtection(corr, SearchMode.ALL).ifPresent {
        it.signs.forEach { linkedSign -> linkedSign.location.block.type = Material.AIR }
    }
}

/**
 * Synchronizes the overlap between sections
 */
object SectionSyncListener : Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun BlockBreakEvent.syncBlockBreak() {
        val block = block
        block.location.sync { original, corr ->
            val state = corr.state

            //if breaking from bottom container, drop items stored in top container here
            if (state is Container && original.location.y > corr.location.y) {
                val corrInv = state.inventory
                if (state is ShulkerBox) {
                    isDropItems = false
                    //TODO maybe create our own event that gets called from here
                    corr.drops.dropItems(original.location, noVelocity = false)
                } else {
                    corrInv.toList().dropItems(original.location, false)
                }
                corrInv.clear()
            }

            //sync any changes to BlockLocker's signs`
            if (DeeperContext.isBlockLockerLoaded && state is Sign && state.lines[0] == "[Private]") {
                syncBlockLocker(corr)
            }
            corr.type = Material.AIR
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun BlockPlaceEvent.syncBlockPlace() {
        block.sync { original, corr ->
            if (original.type.name.contains("SHULKER")) {
                isCancelled = true
                player.error("Shulkers are disabled near section changes due to item loss bugs.")
                return@sync
            }
            corr.blockData = original.blockData.clone()
        }
    }

    //handles fertilized crops as well
    //disabled for now as it causes significant lag.
    /*@EventHandler
    fun syncBlockGrow(blockEvent: BlockGrowEvent) {
        blockEvent.newState.location.sync()
    }*/

    //TODO this causes duplication glitches that need to be fixed first
    /*@EventHandler
    fun onBlockMultiPlaceEvent(blockEvent: BlockMultiPlaceEvent) {
        blockEvent.replacedBlockStates.copyBlocks()
    }*/

    @EventHandler
    fun PlayerBucketEmptyEvent.syncWaterEmpty() =
        block.sync { orig, corr ->
            val data = corr.blockData
            if (data is Waterlogged) {
                data.isWaterlogged = true
                corr.blockData = data
//                    corr.state.update(true, true) //TODO we need to send a block update somehow and this doesn't work
            } else
                updateMaterial(Material.WATER)(orig, corr)
        }

    @EventHandler
    fun PlayerBucketFillEvent.syncWaterFill() =
        block.sync { orig, corr ->
            val data = corr.blockData
            if (data is Waterlogged) {
                data.isWaterlogged = false
                corr.blockData = data
            } else
                updateMaterial(Material.AIR)(orig, corr)
        }

    /** Synchronize explosions */
    @EventHandler
    fun EntityExplodeEvent.syncExplosions() {
        if (!isCancelled)
            blockList().forEach { explodedBlock ->
                explodedBlock.location.sync(updateMaterial(Material.AIR))
            }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun SignChangeEvent.syncSignText() {
        block.sync(signUpdater(lines))
    }
}
