package com.derongan.minecraft.deeperworld.synchronization

import com.derongan.minecraft.deeperworld.DeeperContext
import com.derongan.minecraft.deeperworld.world.section.*
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.logInfo
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import nl.rutgerkok.blocklocker.SearchMode
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.Sign
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack

/**
 * Synchronizes the overlap between sections
 */
object SectionSyncListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onBlockBreakEvent(blockBreakEvent: BlockBreakEvent) {
        val block = blockBreakEvent.block
        updateCorrespondingBlock(block.location) { original, corr ->
            val state = corr.state
            if (state is Container && original.location.y > corr.location.y) { //if this a container in the lower section
                val corrInv = state.inventory
                corrInv.toList().dropItems(original.location, false)
                corrInv.clear()
            }
            if (DeeperContext.isBlockLockerLoaded && state is Sign && state.lines[0] == "[Private]") {
                //TODO ignore blocklocker code if it isn't present
                blockLocker.protectionFinder.findProtection(corr, SearchMode.ALL).ifPresent {
                    it.signs.forEach { linkedSign -> linkedSign.location.block.type = Material.AIR }
                }
            }
            corr.type = Material.AIR
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onBlockPlaceEvent(blockPlaceEvent: BlockPlaceEvent) {
        val block = blockPlaceEvent.block
        val location = block.location
        updateCorrespondingBlock(location, updateBlockData)
    }


    /** Disables pistons if they are in the overlap of two sections */
    @EventHandler
    fun onPistonEvent(event: BlockPistonExtendEvent) {
        if (event.block.location.correspondingSection != null)
            event.isCancelled = true
    }

    @EventHandler
    fun onWaterEmptyEvent(event: PlayerBucketEmptyEvent) =
            updateCorrespondingBlock(event.block.location) { orig, corr ->
                val data = corr.blockData
                if (data is Waterlogged) {
                    data.isWaterlogged = true
                    corr.blockData = data
//                    corr.state.update(true, true) //TODO we need to send a block update somehow and this doesn't work
                } else
                    updateMaterial(Material.WATER)(orig, corr) //TODO this is cursed code and should be done differently
            }

    @EventHandler
    fun onWaterFillEvent(event: PlayerBucketFillEvent) =
            updateCorrespondingBlock(event.block.location) { orig, corr ->
                val data = corr.blockData
                if (data is Waterlogged) {
                    data.isWaterlogged = false
                    corr.blockData = data
                } else
                    updateMaterial(Material.AIR)(orig, corr)
            }

    /** Tells a chunk what players are accessing inventories on its [Section] border*/
    private val keepLoadedInventories = mutableMapOf<Chunk, MutableList<Player>>()

    /** Synchronize container interactions between sections*/
    @EventHandler
    fun onInteractWithContainer(event: PlayerInteractEvent) {
        val clicked = event.clickedBlock ?: return
        val loc = clicked.location
        val container = clicked.state
        val player = event.player
        if (container is Container && event.action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking) {
            val section = loc.section ?: return
            val linkedSection = loc.correspondingSection ?: return
            val linkedBlock = loc.getCorrespondingLocation(section, linkedSection).block

            if (DeeperContext.isBlockLockerLoaded) {
                fun updateProtection(block: Block) = blockLocker.protectionFinder.findProtection(block, SearchMode.ALL).ifPresent {
                    it.signs.forEach { sign -> updateCorrespondingBlock(sign.location, signUpdater()) }
                }
                updateProtection(linkedBlock)
                updateProtection(clicked)

                if (player.inventory.itemInMainHand.type.name.contains("SIGN") //allow chest protection signs to be placed
                        || !BlockLockerAPIv2.isAllowed(player, clicked, true)
                        || !BlockLockerAPIv2.isAllowed(player, linkedBlock, true))
                    return
            }

            if (section.isOnTopOf(linkedSection)) return

            event.isCancelled = true

            //execute only if inventory successfully opened (e.x. not prevented by WorldGuard)
            val linkedInventory = ((linkedBlock.state as? Container) ?: return).inventory
            if (player.openInventory(linkedInventory) != null) {
                logInfo("openeed successfully!")
                //synchronize chests and drop anything that doesn't fit
                val otherInventory = ((linkedBlock.state as? Container) ?: return).inventory
                val invList: List<ItemStack> = container.inventory.toList().filterNotNull()
                if (invList.isNotEmpty()) {
                    //try adding items to the chest above, if something doesn't fit, drop it
                    invList.map { otherInventory.addItem(it).values }.flatten().also {
                        if (it.isNotEmpty())
                            player.sendMessage("&6This container had items in it, which have been ejected to synchronize it with the upper section.".color())
                    }.dropItems(loc, true)
                    container.inventory.clear()
                }

                //add player to map of players using this inventory
                if (!linkedBlock.chunk.isForceLoaded)
                    keepLoadedInventories.getOrPut(linkedBlock.chunk, { mutableListOf() }) += player
            }
        }
    }

    /** If there are any players accessing a synced inventory in this chunk, don't unload it*/
    @EventHandler
    fun onChunkUnload(cue: ChunkUnloadEvent) {
        if (keepLoadedInventories.containsKey(cue.chunk)) cue.chunk.load()
    }

    /** Removes the player from the [keepLoadedInventories] map*/
    @EventHandler
    fun onCloseInventory(ice: InventoryCloseEvent) {
        val inventory = ice.inventory
        val chunk = inventory.location?.chunk ?: return
        if (keepLoadedInventories[chunk]?.remove(ice.player) != null) {
            if (keepLoadedInventories[chunk]?.isEmpty() == true)
                keepLoadedInventories -= chunk
        }
    }

    /** Synchronize explosions */
    @EventHandler
    fun onExplodeEvent(explodeEvent: EntityExplodeEvent) {
        if (!explodeEvent.isCancelled)
            explodeEvent.blockList().forEach { explodedBlock ->
                updateCorrespondingBlock(explodedBlock.location, updateMaterial(Material.AIR))
            }
    }

    /** Synchronize hopper pickups between sections */
    @EventHandler
    fun hopperGrabEvent(e: InventoryPickupItemEvent) {
        updateCorrespondingBlock(e.inventory.location
                ?: return) { original, corresponding ->
            val section = original.location.section ?: return@updateCorrespondingBlock
            val linkedSection = corresponding.location.section ?: return@updateCorrespondingBlock
            if (linkedSection.isOnTopOf(section)) {
                //if there are no leftover items, remove the itemstack
                if ((corresponding.state as? Container)?.inventory?.addItem(e.item.itemStack)?.isEmpty() != false)
                    e.item.remove()
                e.isCancelled = true
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onSignChangeEvent(signChangeEvent: SignChangeEvent) {
        updateCorrespondingBlock(signChangeEvent.block.location, signUpdater(signChangeEvent.lines))
    }
}