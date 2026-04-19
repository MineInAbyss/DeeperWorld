package com.mineinabyss.deeperworld.synchronization

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.event.BlockSyncEvent
import com.mineinabyss.deeperworld.event.SyncType
import com.mineinabyss.deeperworld.sections.SectionRepository
import com.mineinabyss.deeperworld.sections.correspondingLocation
import com.mineinabyss.deeperworld.sections.inSectionOverlap
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.event.block.BlockBreakBlockEvent
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.block.ShulkerBox
import org.bukkit.block.Sign
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.Waterlogged
import org.bukkit.block.data.type.Bed
import org.bukkit.block.data.type.Sapling
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.block.sign.Side
import org.bukkit.entity.EntityType
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

/**
 * Synchronizes the overlap between sections
 */
class SectionSyncListener(
    private val blockLocker: BlockLockerHelpers?,
    private val sections: SectionRepository,
) : Listener {
    private val attachedBlocks = ObjectOpenHashSet(Tag.REPLACEABLE.values.plus(setOf(Material.TORCH, Material.WALL_TORCH, Material.SPORE_BLOSSOM)))
    private val attachedFaces = ObjectOpenHashSet(BlockFace.entries.take(6))

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun BlockBreakEvent.syncBlockBreak() {
        sections.whenLinked(block) { linked ->
            BlockSyncEvent(block, SyncType.BREAK).call {
                val state = linked.state

                //if breaking from bottom container, drop items stored in top container here
                if (state is Container && block.location.y > linked.location.y) {
                    val corrInv = state.inventory
                    if (state is ShulkerBox) {
                        isDropItems = false
                        //TODO maybe create our own event that gets called from here
                        linked.drops.dropItems(block.location, noVelocity = false)
                    } else {
                        corrInv.toList().dropItems(block.location, false)
                    }
                    corrInv.clear()
                }

                //sync any changes to BlockLocker's signs`

                if (blockLocker != null && state is Sign &&
                    (state.getSide(Side.FRONT).lines().first() == Component.text("[Private]")
                            || state.getSide(Side.BACK).lines().first() == Component.text("[Private]"))
                ) {
                    blockLocker.syncBlockLocker(linked)
                }

                // Breaking a block triggering attached block to break
                attachedFaces.filter { block.getRelative(it).type in attachedBlocks }.forEach {
                    if (linked.getRelative(it).type == block.getRelative(it).type) {
                        linked.getRelative(it).type = Material.AIR
                    }
                }

                when (val blockData = block.blockData) {
                    is Bed -> {
                        linked.setType(Material.STONE, false)
                        when (blockData.part) {
                            Bed.Part.FOOT -> linked.location.add(blockData.facing.direction)
                            Bed.Part.HEAD -> linked.location.subtract(blockData.facing.direction)
                        }.block.type = Material.AIR
                    }

                    is Bisected if blockData !is TrapDoor && blockData !is Stairs -> {
                        linked.setType(Material.STONE, false)
                        when (blockData.half) {
                            Bisected.Half.BOTTOM -> linked.location.add(0.0, 1.0, 0.0)
                            Bisected.Half.TOP -> linked.location.subtract(0.0, 1.0, 0.0)
                        }.block.type = Material.AIR
                    }
                }

                linked.type = Material.AIR
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun BlockPlaceEvent.syncBlockPlace() {
        sections.whenLinked(block) { linked ->
            BlockSyncEvent(block, SyncType.PLACE).call {
                linked.blockData = block.blockData
            }
        }
    }

    @EventHandler
    fun BlockGrowEvent.syncBlockGrow() {
        sections.whenLinked(block) { linked ->
            deeperWorld.launch {
                delay(1.ticks)
                linked.blockData = block.blockData
            }
        }
    }

    // Since [BlockGrowEvent] doesn't get called for bonemeal-growth
    @EventHandler
    fun PlayerInteractEvent.syncBlockGrowFromBoneMeal() {
        val block = clickedBlock ?: return

        // Ensure clicked block is growable
        if (action != Action.RIGHT_CLICK_BLOCK || hand != EquipmentSlot.HAND) return
        if (player.inventory.getItem(EquipmentSlot.HAND).type != Material.BONE_MEAL) return
        if (block.blockData !is Ageable || block is Sapling) return

        sections.whenLinked(block) { linked ->
            if (linked.type == block.type) deeperWorld.launch {
                delay(1.ticks)
                linked.blockData = block.blockData
            }
        }
    }

    // Copies structure onto another section
    @EventHandler
    fun StructureGrowEvent.syncStructureGrowth() {
        //TODO verify this actually works
        if (!location.inSectionOverlap) return
        if (blocks.all { (it.block.type == it.block.location.correspondingLocation?.block?.type) })
            sections.forEachLinked(blocks.map { it.block }) { block, linked ->
                linked.blockData = block.blockData
            }
        else isCancelled = true
    }

    @EventHandler
    fun BlockMultiPlaceEvent.syncMultiBlockPlace() {
        if (!block.location.inSectionOverlap) return
        val data = block.blockData
        if ((data is Bisected || data is Bed) && data !is TrapDoor && data !is Stairs)
            sections.forEachLinked(replacedBlockStates.map { it.block }) { block, linked ->
                linked.blockData = block.blockData
            }
    }

    @EventHandler
    fun PlayerBucketEmptyEvent.syncWaterEmpty() {
        if (!block.location.inSectionOverlap) return
        sections.whenLinked(block) { linked ->
            val data = linked.blockData
            val material = if (bucket === Material.LAVA_BUCKET) Material.LAVA else Material.WATER
            if (data is Waterlogged) {
                data.isWaterlogged = true
                // Trigger block update for water
                if (linked.state !is Container) linked.type = material
                linked.type = block.type
                linked.blockData = data
            } else
                linked.type = material
        }
    }


    @EventHandler
    fun PlayerBucketFillEvent.syncWaterFill() {
        sections.whenLinked(block) { linked ->
            val data = linked.blockData
            if (data is Waterlogged) {
                data.isWaterlogged = false
                linked.blockData = data
            } else linked.type = Material.AIR
            //TODO verify setting to air is correct and that just copying blockdata always doesnt work?
        }
    }


    /** Synchronize explosions */
    @EventHandler(ignoreCancelled = true)
    fun EntityExplodeEvent.syncExplosions() {
        sections.forEachLinked(blockList()) { _, linked ->
            linked.type = Material.AIR
        }
    }

    /** Synchronize explosions */
    @EventHandler(ignoreCancelled = true)
    fun BlockExplodeEvent.syncExplosions() {
        sections.forEachLinked(blockList()) { _, linked ->
            linked.type = Material.AIR
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun SignChangeEvent.syncSignText() {
        if (!block.location.inSectionOverlap) return
        sections.whenLinked(block) { linked ->
            updateSign(linked, block, lines())
        }
    }

    @EventHandler
    fun EntityChangeBlockEvent.syncBlockChange() {
        sections.whenLinked(block) { linked ->
            linked.blockData = this.blockData
        }
    }

    @EventHandler
    fun InventoryBlockStartEvent.onFurnaceStart() {
        sections.whenLinked(block) { linked ->
            deeperWorld.launch {
                delay(1.ticks)
                linked.blockData = block.blockData
            }
        }
    }

    /** Removes Iron Golem and Wither summons in corresponding section location due to duping **/
    @EventHandler
    fun EntitySpawnEvent.onEntitySummon() {
        if (entityType != EntityType.WITHER && entityType != EntityType.IRON_GOLEM) return

        sections.whenLinked(entity.location) { linked, _ ->
            linked.getNearbyEntitiesByType(entityType.entityClass, 1.0).firstOrNull()?.remove()
        }
    }

    /*
    /** Sync items removed by void to corresponding section */
    @EventHandler
    fun EntityRemoveFromWorldEvent.onVoidRemoval() {
        val item = (entity as? Item)?.takeIf { it.y < it.world.minHeight } ?: return
        val corrLoc = item.location.apply { y = -240.0 }.correspondingLocation ?: return
        deeperWorld.launch {
            val chunk = corrLoc.world.getChunkAtAsync(corrLoc).await()
            val addedTicket = chunk.addPluginChunkTicket(deeperWorld)
            corrLoc.spawn<Item> {
                itemStack = item.itemStack
                thrower = item.thrower
                owner = item.owner
                velocity = item.velocity
            }
            if (addedTicket) {
                delay(10.seconds)
                chunk.removePluginChunkTicket(deeperWorld)
            }
        }
    }*/
}
