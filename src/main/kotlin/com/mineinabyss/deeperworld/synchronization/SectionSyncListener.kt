package com.mineinabyss.deeperworld.synchronization

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.event.BlockSyncEvent
import com.mineinabyss.deeperworld.event.SyncType
import com.mineinabyss.deeperworld.world.section.correspondingLocation
import com.mineinabyss.deeperworld.world.section.inSectionOverlap
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import nl.rutgerkok.blocklocker.SearchMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.*
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.Waterlogged
import org.bukkit.block.data.type.Bed
import org.bukkit.block.data.type.Sapling
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.block.sign.Side
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.inventory.EquipmentSlot

private fun syncBlockLocker(corr: Block) {
    blockLocker?.protectionFinder?.findProtection(corr, SearchMode.ALL)?.ifPresent {
        it.signs.forEach { linkedSign -> linkedSign.location.block.type = Material.AIR }
    }
}

/**
 * Synchronizes the overlap between sections
 */
object SectionSyncListener : Listener {

    private val attachedBlocks = mutableSetOf(Material.TORCH, Material.WALL_TORCH, Material.SPORE_BLOSSOM).also { it.addAll(Tag.REPLACEABLE.values) }.toSet()
    private val attachedFaces = setOf(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun BlockBreakEvent.syncBlockBreak() {
        if (!block.location.inSectionOverlap) return
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
                if (Plugins.isEnabled("BlockLocker") && state is Sign &&
                    (state.getSide(Side.FRONT).lines().first() == Component.text("[Private]")
                            || state.getSide(Side.BACK).lines().first() == Component.text("[Private]"))) {
                    syncBlockLocker(corr)
                }

                // Breaking a block triggering attached block to break
                attachedFaces.filter { block.getRelative(it).type in attachedBlocks }.forEach {
                    if (corr.getRelative(it).type == block.getRelative(it).type) {
                        corr.getRelative(it).type = Material.AIR
                    }
                }

                val blockData = block.blockData
                when {
                    blockData is Bed -> {
                        corr.setType(Material.STONE, false)
                        when (blockData.part) {
                            Bed.Part.FOOT -> corr.location.add(blockData.facing.direction)
                            Bed.Part.HEAD -> corr.location.subtract(blockData.facing.direction)
                        }.block.type = Material.AIR
                    }
                    blockData is Bisected
                            && blockData !is TrapDoor
                            && blockData !is Stairs -> {
                        corr.setType(Material.STONE, false)
                        when (blockData.half) {
                            Bisected.Half.BOTTOM -> corr.location.add(0.0, 1.0, 0.0)
                            Bisected.Half.TOP -> corr.location.subtract(0.0, 1.0, 0.0)
                        }.block.type = Material.AIR
                    }
                }

                corr.type = Material.AIR

            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun BlockPlaceEvent.syncBlockPlace() {
        if (!block.location.inSectionOverlap) return
        BlockSyncEvent(block, SyncType.PLACE).call {
            block.sync(updateBlockData(block.blockData))
        }
    }

    @EventHandler
    fun BlockGrowEvent.syncBlockGrow() {
        if (!block.location.inSectionOverlap) return
        if (!block.location.inSectionOverlap) return
        deeperWorld.plugin.launch {
            delay(1.ticks)
            block.sync(updateBlockData(block.blockData))
        }
    }

    // Since [BlockGrowEvent] doesn't get called for bonemeal-growth
    @EventHandler
    fun PlayerInteractEvent.syncBlockGrowFromBoneMeal() {
        val block = clickedBlock ?: return
        val corrBlock = block.location.correspondingLocation?.block ?: return

        if (!block.location.inSectionOverlap) return
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (player.inventory.getItem(EquipmentSlot.HAND).type != Material.BONE_MEAL) return
        if (block.blockData !is Ageable || block is Sapling) return
        if (!block.location.inSectionOverlap || corrBlock.type != block.type) return

        deeperWorld.plugin.launch {
            delay(1.ticks)
            block.sync(updateBlockData(block.blockData))
        }
    }

    // Copies structure onto another section
    @EventHandler
    fun StructureGrowEvent.syncStructureGrowth() {
        if (!location.inSectionOverlap) return
        if (blocks.all { (it.block.type == it.block.location.correspondingLocation?.block?.type) })
            blocks.forEach { it.block.sync(updateBlockData(it.blockData)) }
        else isCancelled = true
    }

    @EventHandler
    fun BlockMultiPlaceEvent.syncMultiBlockPlace() {
        if (!block.location.inSectionOverlap) return
        val data = block.blockData
        if (
            (data is Bisected || data is Bed)
            && data !is TrapDoor
            && data !is Stairs
        ) replacedBlockStates.forEach { it.block.sync() }
    }

    @EventHandler
    fun PlayerBucketEmptyEvent.syncWaterEmpty() {
        if (!block.location.inSectionOverlap) return
        block.sync { orig, corr ->
            val data = corr.blockData
            val material = if (bucket === Material.LAVA_BUCKET) Material.LAVA else Material.WATER
            if (data is Waterlogged) {
                data.isWaterlogged = true
                // Trigger block update for water
                if (corr.state !is Container) corr.type = material
                corr.type = orig.type
                corr.blockData = data
            } else
                updateMaterial(material)(orig, corr)
        }
    }


    @EventHandler
    fun PlayerBucketFillEvent.syncWaterFill() {
        if (!block.location.inSectionOverlap) return
        block.sync { orig, corr ->
            val data = corr.blockData
            if (data is Waterlogged) {
                data.isWaterlogged = false
                corr.blockData = data
            } else
                updateMaterial(Material.AIR)(orig, corr)
        }
    }


    /** Synchronize explosions */
    @EventHandler(ignoreCancelled = true)
    fun EntityExplodeEvent.syncExplosions() {
        blockList().forEach { explodedBlock ->
            explodedBlock.location.sync(updateMaterial(Material.AIR))
        }
    }

    /** Synchronize explosions */
    @EventHandler(ignoreCancelled = true)
    fun BlockExplodeEvent.syncExplosions() {
        blockList().forEach { explodedBlock ->
            explodedBlock.location.sync(updateMaterial(Material.AIR))
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun SignChangeEvent.syncSignText() {
        if (!block.location.inSectionOverlap) return
        block.sync(signUpdater(lines()))
    }

    @EventHandler
    fun EntityChangeBlockEvent.syncBlockChange() {
        if (!block.location.inSectionOverlap) return
        block.sync(updateBlockData(blockData))
    }

    @EventHandler
    fun InventoryBlockStartEvent.onFurnaceStart() {
        if (!block.location.inSectionOverlap) return
        deeperWorld.plugin.launch {
            delay(1.ticks)
            block.sync(updateBlockData(block.blockData))
        }
    }

    /** Removes Iron Golem and Wither summons in corresponding section location due to duping **/
    @EventHandler
    fun EntitySpawnEvent.onEntitySummon() {
        val corrLocation = entity.location.correspondingLocation ?: return
        if (entityType != EntityType.WITHER && entityType != EntityType.IRON_GOLEM) return

        entity.world.getNearbyEntitiesByType(entityType.entityClass, corrLocation, 1.0).firstOrNull()?.remove()
    }

    /** Sync items removed by void to corresponding section */
    @EventHandler
    fun EntityRemoveFromWorldEvent.onVoidRemoval() {
        val item = (entity as? Item)?.takeIf { it.y < it.world.minHeight } ?: return
        val corrLoc = item.location.apply { y = -240.0 }.correspondingLocation ?: return
        corrLoc.spawn<Item> {
            itemStack = item.itemStack
            thrower = item.thrower
            owner = item.owner
            velocity = velocity
        }
    }
}
