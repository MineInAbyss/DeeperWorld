package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.player.PlayerManager
import com.derongan.minecraft.deeperworld.world.WorldManager
import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.deeperworld.world.section.SectionKey
import com.derongan.minecraft.deeperworld.world.section.SectionUtils
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack

class MovementListener(private val playerManager: PlayerManager) : Listener {
    private val worldManager: WorldManager = Bukkit.getServicesManager().load(WorldManager::class.java)!!
    private val updateBlockData = { original: Block, corresponding: Block ->
        corresponding.blockData = original.blockData.clone()
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onPlayerMove(playerMoveEvent: PlayerMoveEvent) {
        val player = playerMoveEvent.player
        if (player.hasPermission(Permissions.CHANGE_SECTION_PERMISSION) && playerManager.playerCanTeleport(player)) {
            onPlayerMoveInternal(player, playerMoveEvent.from, playerMoveEvent.to)
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onBlockBreakEvent(blockBreakEvent: BlockBreakEvent) {
        val block = blockBreakEvent.block

        updateCorrespondingBlock(block.location) { original, corr ->
            val container = corr.state
            if (container is Container && original.location.y > corr.location.y) { //if this a container in the lower section
                container.inventory.contents
                container.inventory.toList().filterNotNull().dropItems(original.location, false)
                container.inventory.clear()
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
        if (event.block.location.getSectionLinkedTo() != null)
            event.isCancelled = true
    }

    @EventHandler
    fun onWaterEmptyEvent(event: PlayerBucketEmptyEvent) {
        updateCorrespondingBlock(event.block.location) { _, corr ->
            corr.type = Material.WATER
        }
    }

    @EventHandler
    fun onWaterFillEvent(event: PlayerBucketFillEvent) {
        updateCorrespondingBlock(event.block.location) { _, corr ->
            corr.type = Material.AIR
        }
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
            val linkedSection = loc.getSectionLinkedTo(section) ?: return
            val corresponding = SectionUtils.getCorrespondingLocation(section, linkedSection, loc)
            val linkedBlock = corresponding.block
            if (section.belowKey == linkedSection.key) return
            event.isCancelled = true
            val inventory = container.inventory.toList().filterNotNull()
            if (inventory.isNotEmpty()) {
                inventory.dropItems(loc, true)
                container.inventory.clear()
                player.sendMessage("${ChatColor.GOLD}This container wasn't synchronized correctly, its items have been dropped, it will function normally from now on!")
                return
            }
            player.openInventory((linkedBlock.state as Container).inventory)
        }
    }

    private fun List<ItemStack>.dropItems(loc: Location, noVelocity: Boolean) {
        val spawnLoc = loc.clone().add(0.5, if (noVelocity) 1.0 else 0.0, 0.5)
        forEach { loc.world?.dropItem(spawnLoc, it).apply { if (noVelocity) this?.velocity = org.bukkit.util.Vector(0, 0, 0) } }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onSignChangeEvent(signChangeEvent: SignChangeEvent) {
        val block = signChangeEvent.block
        val location = block.location
        updateCorrespondingBlock(location) { orig, corr ->
            updateBlockData(orig, corr)
            if (corr.state is Sign) {
                val sign = corr.state as Sign
                if (!sign.lines.contentEquals(signChangeEvent.lines)) {
                    for (i in signChangeEvent.lines.indices) {
                        sign.setLine(i, signChangeEvent.getLine(i)!!)
                    }
                }
                sign.update()
            }
        }
    }

    //TODO put into some actual helper class
    fun Location.getSectionLinkedTo(section: Section? = this.section): Section? {
        if (section == null) return null

        val above = worldManager.getSectionFor(section.aboveKey)
        val below = worldManager.getSectionFor(section.belowKey)
        return when {
            above != null && SectionUtils.isSharedLocation(section, above, this) -> above
            below != null && SectionUtils.isSharedLocation(section, below, this) -> below
            else -> null
        }
    }

    fun Location.getLocationFromLinkedSection(linkedSection: Section? = null): Location? {
        val section = this.section ?: return null

        return SectionUtils.getCorrespondingLocation(section, linkedSection ?: getSectionLinkedTo(section), this)
    }

    val Location.section: Section? get() = worldManager.getSectionFor(this)

    private fun updateCorrespondingBlock(original: Location, updater: (original: Block, corresponding: Block) -> Unit) {
        val section = original.section
        val toSection: Section = original.getSectionLinkedTo(section) ?: return
        val corresponding: Location = original.getLocationFromLinkedSection(toSection) ?: return
        //ensure blocks don't get altered outside of the corresponding region
        if (toSection.region.contains(corresponding.blockX, corresponding.blockZ))
            updater(original.block, corresponding.block)
    }

    private fun onPlayerMoveInternal(player: Player, from: Location, to: Location?) {
        to ?: return
        val changeY = to.y - from.y
        if (changeY == 0.0) return

        val inSpectator = player.gameMode == GameMode.SPECTATOR
        val current = worldManager.getSectionFor(player.location) ?: return

        fun tpIfAbleTo(key: SectionKey, tpFun: (Player, Location?, Section, Section) -> Unit, boundaryCheck: (y: Double, shared: Int) -> Boolean, pushVelocity: Double) {
            val toSection = worldManager.getSectionFor(key) ?: return
            val shared = SectionUtils.getSharedBlocks(current, toSection)
            val correspondingPos = SectionUtils.getCorrespondingLocation(current, toSection, to)

            if (boundaryCheck(to.y, shared)) {
                if (!toSection.region.contains(correspondingPos.blockX, correspondingPos.blockZ)
                        || !inSpectator && correspondingPos.block.type.isSolid)
                    player.velocity = player.velocity.setY(pushVelocity)
                else
                    tpFun(player, to, current, toSection)
            }
        }

        when {
            changeY > 0.0 -> tpIfAbleTo(current.aboveKey, ::ascend, { y, shared -> y > MinecraftConstants.WORLD_HEIGHT - .3 * shared }, -0.4)
            changeY < 0.0 -> tpIfAbleTo(current.belowKey, ::descend, { y, shared -> y < .3 * shared }, 0.4)
        }
    }

    private fun descend(player: Player, to: Location?, oldSection: Section, newSection: Section) {
        val event = PlayerDescendEvent(player, oldSection, newSection)
        Bukkit.getServer().pluginManager.callEvent(event)
        if (!event.isCancelled) {
            teleportBetweenSections(player, to, oldSection, newSection)
        }
    }

    private fun ascend(player: Player, to: Location?, oldSection: Section, newSection: Section) {
        val event = PlayerAscendEvent(player, oldSection, newSection)
        Bukkit.getServer().pluginManager.callEvent(event)
        if (!event.isCancelled) {
            teleportBetweenSections(player, to, oldSection, newSection)
        }
    }

    private fun teleportBetweenSections(player: Player, to: Location?, oldSection: Section, newSection: Section) {
        val newLoc = SectionUtils.getCorrespondingLocation(oldSection, newSection, to)
        val fallDistance = player.fallDistance
        val oldVelocity = player.velocity
        player.teleport(newLoc)
        player.fallDistance = fallDistance
        player.velocity = oldVelocity
    }
}