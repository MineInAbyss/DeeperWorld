package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.world.section.*
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import nl.rutgerkok.blocklocker.BlockLockerPlugin
import nl.rutgerkok.blocklocker.SearchMode
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.Sign
import org.bukkit.block.data.Waterlogged
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector


class SectionSyncListener : Listener {
    var blockLocker: BlockLockerPlugin = BlockLockerAPIv2.getPlugin()
    private val updateBlockData = { original: Block, corresponding: Block ->
        corresponding.blockData = original.blockData.clone()
    }

    private fun updateCorrespondingBlock(original: Location, updater: (original: Block, corresponding: Block) -> Unit) {
        val corresponding: Location = original.correspondingLocation ?: return
        //ensure blocks don't get altered when we are outside of the corresponding region
        if (corresponding.inSectionOverlap)
            updater(original.block, corresponding.block)
    }

    private fun updateMaterial(material: Material) = { _: Block, corr: Block -> corr.type = material }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onBlockBreakEvent(blockBreakEvent: BlockBreakEvent) {
        val block = blockBreakEvent.block

        updateCorrespondingBlock(block.location) { original, corr ->
            val state = corr.state
            if (state is Container && original.location.y > corr.location.y) { //if this a container in the lower section
                //only drops items that aren't matched between containers TODO haven't tested ingame yet
                state.inventory.toList().dropItems(original.location, false, (corr.state as Container).inventory.toList())
                state.inventory.clear()
            }
            if (state is Sign && state.lines[0] == "[Private]") {
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


    /**
     * Disables pistons if they are in the overlap of two sections
     */
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

    //TODO move out all the section syncing events into a different listener
    //TODO doesn't work between worlds
    @EventHandler
    fun onInteractWithChest(event: PlayerInteractEvent) {
        val clicked = event.clickedBlock ?: return
        val loc = clicked.location
        val container = clicked.state
        val player = event.player
        if (container is Container && event.action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking) {
            val section = loc.section ?: return
            val linkedSection = loc.correspondingSection ?: return
            val linkedBlock = loc.getCorrespondingLocation(section, linkedSection).block

            fun updateProtection(block: Block) =
                    blockLocker.protectionFinder.findProtection(block, SearchMode.ALL).ifPresent {
                        it.signs.forEach { sign -> updateCorrespondingBlock(sign.location, signUpdater()) }
                    }
            updateProtection(linkedBlock)
            updateProtection(clicked)

            if (section.isOnTopOf(linkedSection) || player.inventory.itemInMainHand.type.name.contains("SIGN")) return

            if (!BlockLockerAPIv2.isAllowed(player, clicked, true) || !BlockLockerAPIv2.isAllowed(player, linkedBlock, true))
                return

            event.isCancelled = true
            val inventory: List<ItemStack?> = container.inventory.toList()
            if (inventory.isNotEmpty()) {
                container.inventory
                inventory.dropItems(loc, true, (linkedBlock.state as Container).inventory.toList())
                container.inventory.clear()
                player.sendMessage("${ChatColor.GOLD}This container had items in it, which have been ejected to synchronize it with the upper section. Hoppers may cause this!")
                return
            }
            player.openInventory((linkedBlock.state as Container).inventory)
        }
    }

    private fun List<ItemStack?>.dropItems(loc: Location, noVelocity: Boolean, compareTo: List<ItemStack?>) {
        val spawnLoc = loc.clone().add(0.5, if (noVelocity) 1.0 else 0.0, 0.5)
        filterIndexed { i, it -> compareTo[i] != it } //don't drop items that are in the same slot
                .filterNotNull()
                .forEach { loc.world?.dropItem(spawnLoc, it).apply { if (noVelocity) this?.velocity = Vector(0, 0, 0) } }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onSignChangeEvent(signChangeEvent: SignChangeEvent) {
        updateCorrespondingBlock(signChangeEvent.block.location, signUpdater(signChangeEvent.lines))
    }

    private fun signUpdater(lines: Array<String>? = null) = { original: Block, corresponding: Block ->
        updateBlockData(original, corresponding)
        val sign = original.state
        if (sign is Sign) {
            val readLines = lines ?: sign.lines
            val corrSign = corresponding.state
            if (corrSign is Sign && !corrSign.lines.contentEquals(readLines)) {
                readLines.forEachIndexed { i, line -> corrSign.setLine(i, line) }
                corrSign.update()
            }
        }
    }
}