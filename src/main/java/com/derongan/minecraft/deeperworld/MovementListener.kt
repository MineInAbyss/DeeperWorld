package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.player.PlayerManager
import com.derongan.minecraft.deeperworld.world.WorldManager
import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.deeperworld.world.section.SectionKey
import com.derongan.minecraft.deeperworld.world.section.SectionUtils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerMoveEvent

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
        updateCorrespondingBlock(block.location) { _, corr ->
            corr.type = Material.AIR
        }
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onBlockPlaceEvent(blockPlaceEvent: BlockPlaceEvent) {
        val block = blockPlaceEvent.block
        val location = block.location
        updateCorrespondingBlock(location, updateBlockData)
    }

    private fun updateCorrespondingBlock(original: Location, updater: (original: Block, corresponding: Block) -> Unit) {
        val section = worldManager.getSectionFor(original) ?: return
        val above = worldManager.getSectionFor(section.aboveKey)
        val below = worldManager.getSectionFor(section.belowKey)

        val toSection: Section = when {
            above != null && SectionUtils.isSharedLocation(section, above, original) -> above
            below != null && SectionUtils.isSharedLocation(section, below, original) -> below
            else -> null
        } ?: return
        val corresponding: Location = SectionUtils.getCorrespondingLocation(section, toSection, original)
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
                        || !inSpectator && !correspondingPos.block.isPassable)
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