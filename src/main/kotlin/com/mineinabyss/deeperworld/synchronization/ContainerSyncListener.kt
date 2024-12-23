package com.mineinabyss.deeperworld.synchronization

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.world.section.*
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import nl.rutgerkok.blocklocker.SearchMode
import org.bukkit.Chunk
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.DecoratedPot
import org.bukkit.block.Lidded
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


object ContainerSyncListener : Listener {

    /** Tells a chunk what players are accessing inventories on its [Section] border */
    private val keepLoadedInventories = mutableMapOf<Chunk, MutableList<Player>>()

    /** Synchronize container interactions between sections */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerInteractEvent.onInteractWithContainer() {
        val (block, container) = (clickedBlock ?: return) to (clickedBlock?.state as? Container ?: return)
        if (action != Action.RIGHT_CLICK_BLOCK || player.isSneaking) return

        val section = block.location.section ?: return
        val linkedSection = block.location.correspondingSection ?: return
        val linkedBlock = block.location.getCorrespondingLocation(section, linkedSection)?.block ?: return

        blockLocker?.apply {
            updateProtection(linkedBlock)
            updateProtection(block)

            //allow chest protection signs to be placed
            if (player.inventory.itemInMainHand.type.name.contains("SIGN")
                || !BlockLockerAPIv2.isAllowed(player, block, true)
                || !BlockLockerAPIv2.isAllowed(player, linkedBlock, true)
            ) return
        }

        if (container is Lidded) {
            (linkedBlock.state as Lidded).open()
            if (!section.isOnTopOf(linkedSection)) (container as Lidded).open()
        }

        if (section.isOnTopOf(linkedSection)) return

        isCancelled = true

        val linkedInventory = ((linkedBlock.state as? Container) ?: return).inventory

        //execute only if inventory successfully opened (e.x. not prevented by WorldGuard)
        if (player.openInventory(linkedInventory) != null) {
            //synchronize chests and drop anything that doesn't fit
            val invItems: List<ItemStack> = container.inventory.toList().filterNotNull()
            if (invItems.isNotEmpty()) {
                //try adding items to the chest above, if something doesn't fit, drop it
                invItems.map { linkedInventory.addItem(it).values }.flatten().also {
                    if (it.isNotEmpty())
                        player.info("<gold>This container had items in it, which have been ejected to synchronize it with the upper section.")
                }.dropItems(block.location, true)
                container.inventory.clear()
            }

            //keep chunk loaded
            linkedBlock.chunk.addPluginChunkTicket(deeperWorld.plugin)

            //keep track of players opening inventory in this chunk
            keepLoadedInventories.getOrPut(linkedBlock.chunk) { mutableListOf() } += player
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.onDecoratedPotFill() {
        val (block, pot) = (clickedBlock ?: return) to (clickedBlock?.state as? DecoratedPot ?: return)
        if (action != Action.RIGHT_CLICK_BLOCK || player.isSneaking) return

        val section = block.location.section ?: return
        val linkedSection = block.location.correspondingSection ?: return
        val linkedBlock =
            block.location.getCorrespondingLocation(section, linkedSection)?.block?.state as? DecoratedPot ?: return

        deeperWorld.plugin.launch {
            delay(1.ticks)
            linkedBlock.inventory.contents = pot.inventory.contents
        }
    }

    /** Removes the player from the [keepLoadedInventories] map */
    @EventHandler
    fun InventoryCloseEvent.onCloseInventory() {
        inventory.location?.block?.sync { original, corr ->
            if (original.state is Lidded) {
                (original.state as Lidded).close()
                (corr.state as Lidded).close()
            }
        }

        val chunk = inventory.location?.chunk ?: return
        if (keepLoadedInventories[chunk]?.remove(player) != null) {
            if (keepLoadedInventories[chunk]?.isEmpty() == true) {
                keepLoadedInventories -= chunk
                chunk.removePluginChunkTicket(deeperWorld.plugin)
            }
        }
    }

    /** Synchronize hopper pickups between sections */
    @EventHandler(ignoreCancelled = true)
    fun InventoryPickupItemEvent.hopperGrabEvent() {
        inventory.location?.sync { _, corresponding, section, corrSection ->
            if (corrSection.isOnTopOf(section)) {
                //if there are no leftover items, remove the itemstack
                if ((corresponding.state as? Container)?.inventory?.addItem(item.itemStack)?.isEmpty() != false)
                    item.remove()
                isCancelled = true
            }
        }
    }
}
