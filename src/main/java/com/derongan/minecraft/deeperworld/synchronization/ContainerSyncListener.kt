package com.derongan.minecraft.deeperworld.synchronization

import com.derongan.minecraft.deeperworld.DeeperContext
import com.derongan.minecraft.deeperworld.deeperWorld
import com.derongan.minecraft.deeperworld.world.section.*
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.info
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import nl.rutgerkok.blocklocker.SearchMode
import org.bukkit.Chunk
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

private fun updateProtection(block: Block) =
    blockLocker.protectionFinder.findProtection(block, SearchMode.ALL).ifPresent {
        it.signs.forEach { sign -> sign.location.sync(signUpdater()) }
    }

object ContainerSyncListener : Listener {

    /** Tells a chunk what players are accessing inventories on its [Section] border */
    private val keepLoadedInventories = mutableMapOf<Chunk, MutableList<Player>>()

    /** Synchronize container interactions between sections */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInteractWithContainer(event: PlayerInteractEvent) {
        val clicked = event.clickedBlock ?: return
        val loc = clicked.location
        val container = clicked.state
        val (player) = event

        if (container is Container && event.action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking) {
            val section = loc.section ?: return
            val linkedSection = loc.correspondingSection ?: return
            val linkedBlock = loc.getCorrespondingLocation(section, linkedSection)?.block ?: return

            if (DeeperContext.isBlockLockerLoaded) {
                updateProtection(linkedBlock)
                updateProtection(clicked)

                //allow chest protection signs to be placed
                if (player.inventory.itemInMainHand.type.name.contains("SIGN")
                    || !BlockLockerAPIv2.isAllowed(player, clicked, true)
                    || !BlockLockerAPIv2.isAllowed(player, linkedBlock, true)
                )
                    return
            }

            if (section.isOnTopOf(linkedSection)) return

            event.isCancelled = true

            val linkedInventory = ((linkedBlock.state as? Container) ?: return).inventory

            //execute only if inventory successfully opened (e.x. not prevented by WorldGuard)
            if (player.openInventory(linkedInventory) != null) {
                //synchronize chests and drop anything that doesn't fit
                val invItems: List<ItemStack> = container.inventory.toList().filterNotNull()
                if (invItems.isNotEmpty()) {
                    //try adding items to the chest above, if something doesn't fit, drop it
                    invItems.map { linkedInventory.addItem(it).values }.flatten().also {
                        if (it.isNotEmpty())
                            player.info("&6This container had items in it, which have been ejected to synchronize it with the upper section.".color())
                    }.dropItems(loc, true)
                    container.inventory.clear()
                }

                //keep chunk loaded
                linkedBlock.chunk.addPluginChunkTicket(deeperWorld)

                //keep track of players opening inventory in this chunk
                keepLoadedInventories.getOrPut(linkedBlock.chunk, { mutableListOf() }) += player
            }
        }
    }

    /** Removes the player from the [keepLoadedInventories] map */
    @EventHandler
    fun onCloseInventory(e: InventoryCloseEvent) {
        val (inventory) = e
        val chunk = inventory.location?.chunk ?: return
        if (keepLoadedInventories[chunk]?.remove(e.player) != null) {
            if (keepLoadedInventories[chunk]?.isEmpty() == true) {
                keepLoadedInventories -= chunk
                chunk.removePluginChunkTicket(deeperWorld)
            }
        }
    }

    /** Synchronize hopper pickups between sections */
    @EventHandler
    fun hopperGrabEvent(e: InventoryPickupItemEvent) {
        (e.inventory.location ?: return).sync { _, corresponding, section, corrSection ->
            if (corrSection.isOnTopOf(section)) {
                //if there are no leftover items, remove the itemstack
                if ((corresponding.state as? Container)?.inventory?.addItem(e.item.itemStack)?.isEmpty() != false)
                    e.item.remove()
                e.isCancelled = true
            }
        }
    }
}