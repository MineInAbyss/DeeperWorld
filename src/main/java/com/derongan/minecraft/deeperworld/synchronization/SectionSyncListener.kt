package com.derongan.minecraft.deeperworld.synchronization

import com.derongan.minecraft.deeperworld.DeeperContext
import com.derongan.minecraft.deeperworld.event.BlockSyncEvent
import com.derongan.minecraft.deeperworld.event.SyncType
import com.derongan.minecraft.deeperworld.world.section.inSectionOverlap
import com.mineinabyss.idofront.events.call
import com.derongan.minecraft.deeperworld.world.section.correspondingSection
import com.derongan.minecraft.deeperworld.world.section.isOnTopOf
import com.derongan.minecraft.deeperworld.world.section.section
import com.mineinabyss.idofront.messaging.error
import nl.rutgerkok.blocklocker.SearchMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.ShulkerBox
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.Waterlogged
import org.bukkit.block.data.type.Bed
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntitySpawnEvent
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
        BlockSyncEvent(block, SyncType.BREAK).call {
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

                val blockData = block.blockData

                if (blockData is Bed) {
                    corr.setType(Material.STONE, false)
                    when (blockData.part) {
                        Bed.Part.FOOT -> corr.location.add(blockData.facing.direction)
                        Bed.Part.HEAD -> corr.location.subtract(blockData.facing.direction)
                    }.block.type = Material.AIR
                } else if (
                    blockData is Bisected
                    && blockData !is TrapDoor
                    && blockData !is Stairs
                ) {
                    corr.setType(Material.STONE, false)
                    when (blockData.half) {
                        Bisected.Half.BOTTOM -> corr.location.add(0.0, 1.0, 0.0)
                        Bisected.Half.TOP -> corr.location.subtract(0.0, 1.0, 0.0)
                    }.block.type = Material.AIR
                }

                corr.type = Material.AIR

            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun BlockPlaceEvent.syncBlockPlace() {
        BlockSyncEvent(block, SyncType.PLACE).call {
            block.sync { original, corr ->
                if (original.type.name.contains("SHULKER")) {
                    isCancelled = true
                    player.error("Shulkers are disabled near section changes due to item loss bugs.")
                    return@sync
                }
                corr.blockData = original.blockData.clone()
            }
        }
    }

    //handles fertilized crops as well
    //disabled for now as it causes significant lag.
    /*@EventHandler
    fun syncBlockGrow(blockEvent: BlockGrowEvent) {
        blockEvent.newState.location.sync()
    }*/


    @EventHandler
    fun BlockPhysicsEvent.sync() {
        val section = block.location.section ?: return
        val section2 = block.location.correspondingSection ?: return

        if(
            block.location.inSectionOverlap
            && section.isOnTopOf(section2)
            && block.blockData !is Levelled // Water / Lava
        ) isCancelled = true

        if(!section.isOnTopOf(section2)) {
            block.sync()
        }
    }

    @EventHandler
    fun BlockMultiPlaceEvent.syncMultiBlockPlace() {
        if(
            (block.blockData is Bisected || block.blockData is Bed)
            && block.blockData !is TrapDoor
            && block.blockData !is Stairs
        ) {
            for (blockState in replacedBlockStates) {
                blockState.block.sync()
            }
        }
    }

    @EventHandler
    fun PlayerBucketEmptyEvent.syncWaterEmpty() =
        block.sync { orig, corr ->
            val data = corr.blockData
            val material = if (bucket === Material.LAVA_BUCKET) Material.LAVA else Material.WATER
            if (data is Waterlogged) {
                data.isWaterlogged = true
                // Trigger block update for water
                if(corr.state !is Container) corr.type = material
                corr.type = orig.type
                corr.blockData = data
            } else
                updateMaterial(material)(orig, corr)
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

    /** Disables Iron Golem and Wither summons in section transitions due to duping **/
    @EventHandler
    fun EntitySpawnEvent.onEntitySummon() {
        if (entity.location.inSectionOverlap &&
           (entityType == EntityType.WITHER || entityType == EntityType.IRON_GOLEM)) {
            isCancelled = true
            entity.location.getNearbyPlayers(5.0).forEach {
                it.error("Spawning of $entityType is disabled in section overlaps.")
            }
        }
    }
}
