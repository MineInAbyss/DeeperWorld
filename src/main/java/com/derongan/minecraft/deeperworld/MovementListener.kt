package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.services.canMoveSections
import com.derongan.minecraft.deeperworld.world.section.*
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.color
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

object MovementListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onPlayerMove(playerMoveEvent: PlayerMoveEvent) {
        val (player) = playerMoveEvent
        if (player.hasPermission(Permissions.CHANGE_SECTION_PERMISSION) && player.canMoveSections) {
            onPlayerMoveInternal(player, playerMoveEvent.from, playerMoveEvent.to ?: return)
        }
    }

    private fun onPlayerMoveInternal(player: Player, from: Location, to: Location) {
        val current = worldManager.getSectionFor(player.location) ?: let {
            //damage players outside of sections
            if (DeeperContext.damagePlayersOutsideSections > 0.0 && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)) {
                player.damage(0.01) //give a damage effect
                player.health = (player.health - DeeperContext.damagePlayersOutsideSections / 10).coerceAtLeast(0.0) //ignores armor
                player.sendTitle("&cYou are not in a managed section".color(), "&7You will take damage upon moving!".color(), 0, 20, 10)
            }
            return
        }
        val changeY = to.y - from.y
        if (changeY == 0.0) return

        val inSpectator = player.gameMode == GameMode.SPECTATOR

        fun tpIfAbleTo(key: SectionKey, tpFun: (Player, Location, Section, Section) -> Unit, boundaryCheck: (y: Double, shared: Int) -> Boolean, pushVelocity: Double) {
            val toSection = worldManager.getSectionFor(key) ?: return
            val overlap = current.overlapWith(toSection) ?: return
            val correspondingPos = to.getCorrespondingLocation(current, toSection) ?: return

            if (boundaryCheck(to.y, overlap)) {
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
        PlayerDescendEvent(player, oldSection, newSection).call {
            teleportBetweenSections(player, to, oldSection, newSection)
        }
    }

    private fun ascend(player: Player, to: Location, oldSection: Section, newSection: Section) {
        PlayerAscendEvent(player, oldSection, newSection).call {
            teleportBetweenSections(player, to, oldSection, newSection)
        }
    }

    private fun teleportBetweenSections(player: Player, to: Location, oldSection: Section, newSection: Section) {
        val newLoc = to.getCorrespondingLocation(oldSection, newSection) ?: return
        val fallDistance = player.fallDistance
        val oldVelocity = player.velocity
        player.teleport(newLoc)
        player.fallDistance = fallDistance
        player.velocity = oldVelocity
    }
}
