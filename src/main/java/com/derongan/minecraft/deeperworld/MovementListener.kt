package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.player.PlayerManager
import com.derongan.minecraft.deeperworld.world.section.*
import com.mineinabyss.idofront.messaging.color
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MovementListener(private val playerManager: PlayerManager) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onPlayerMove(playerMoveEvent: PlayerMoveEvent) {
        val player = playerMoveEvent.player
        if (player.hasPermission(Permissions.CHANGE_SECTION_PERMISSION) && playerManager.playerCanTeleport(player)) {
            onPlayerMoveInternal(player, playerMoveEvent.from, playerMoveEvent.to)
        }
    }

    private fun onPlayerMoveInternal(player: Player, from: Location, to: Location?) {
        val current = worldManager.getSectionFor(player.location) ?: let {
            if (DeeperContext.damagePlayersOutsideSections >= 0.0 && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)) {
                player.damage(0.01) //give a damage effect
                player.health = (player.health - DeeperContext.damagePlayersOutsideSections/10).coerceAtLeast(0.0) //ignores armor
                player.sendTitle("&cYou are not in a managed section".color(), "&7You will take damage upon moving!".color(), 0, 20, 10)
            }
            return
        }
        to ?: return
        val changeY = to.y - from.y
        if (changeY == 0.0) return

        val inSpectator = player.gameMode == GameMode.SPECTATOR

        fun tpIfAbleTo(key: SectionKey, tpFun: (Player, Location, Section, Section) -> Unit, boundaryCheck: (y: Double, shared: Int) -> Boolean, pushVelocity: Double) {
            val toSection = worldManager.getSectionFor(key) ?: return
            val shared = getSharedBlocks(current, toSection)
            val correspondingPos = to.getCorrespondingLocation(current, toSection)

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

    private fun descend(player: Player, to: Location, oldSection: Section, newSection: Section) {
        val event = PlayerDescendEvent(player, oldSection, newSection)
        Bukkit.getServer().pluginManager.callEvent(event)
        if (!event.isCancelled) {
            teleportBetweenSections(player, to, oldSection, newSection)
        }
    }

    private fun ascend(player: Player, to: Location, oldSection: Section, newSection: Section) {
        val event = PlayerAscendEvent(player, oldSection, newSection)
        Bukkit.getServer().pluginManager.callEvent(event)
        if (!event.isCancelled) {
            teleportBetweenSections(player, to, oldSection, newSection)
        }
    }

    private fun teleportBetweenSections(player: Player, to: Location, oldSection: Section, newSection: Section) {
        val newLoc = to.getCorrespondingLocation(oldSection, newSection)
        val fallDistance = player.fallDistance
        val oldVelocity = player.velocity
        player.teleport(newLoc)
        player.fallDistance = fallDistance
        player.velocity = oldVelocity
    }
}