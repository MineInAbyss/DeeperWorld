package com.derongan.minecraft.deeperworld.movement

import com.derongan.minecraft.deeperworld.config.DeeperConfig
import com.derongan.minecraft.deeperworld.movement.transition.ConfigSectionChecker
import com.derongan.minecraft.deeperworld.movement.transition.SectionTransition
import com.derongan.minecraft.deeperworld.movement.transition.TransitionKind
import com.derongan.minecraft.deeperworld.movement.transition.toEvent
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.color
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

object MovementHandler {
    private val sectionCheckers = listOf(ConfigSectionChecker)

    fun handleMovement(player: Player, from: Location, to: Location) {

        if (sectionCheckers.any { it.inSection(player) })
            sectionCheckers.firstNotNullOfOrNull { it.checkForTransition(player, from, to) }?.let {
                with(getTeleportHandler(player, it)) {
                    if (this.isValidTeleport()) {
                        it.toEvent(player).call {
                            this@with.handleTeleport()
                        }
                    } else {
                        this.handleTeleport()
                    }
                }
            } else {
            applyOutOfBoundsDamage(player)
        }
    }

    //TODO abstract this away. Should instead do out of bounds action if out of bounds.
    private fun applyOutOfBoundsDamage(player: Player) {
        if (DeeperConfig.data.damageOutsideSections > 0.0
            && player.location.world !in DeeperConfig.data.damageExcludedWorlds
            && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)
            && player.location.world in (DeeperConfig.data.worlds)
        ) {
            player.damage(0.01) //give a damage effect
            player.health = (player.health - DeeperConfig.data.damageOutsideSections / 10)
                .coerceIn(
                    0.0,
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value
                ) //ignores armor
            player.sendTitle(
                "&cYou are not in a managed section".color(),
                "&7You will take damage upon moving!".color(),
                0, 20, 10
            )
        }
    }


    private fun getTeleportHandler(
        player: Player,
        sectionTransition: SectionTransition
    ): TeleportHandler {
        if (player.gameMode != GameMode.SPECTATOR && sectionTransition.to.block.isSolid) {
            return if (sectionTransition.kind == TransitionKind.ASCEND) {
                UndoMovementInvalidTeleportHandler(
                    player,
                    sectionTransition.from,
                    sectionTransition.to
                )
            } else {
                BedrockBlockingInvalidTeleportHandler(
                    player,
                    sectionTransition.from,
                    sectionTransition.to
                )
            }
        }

        return TransitionTeleportHandler(player, sectionTransition.from, sectionTransition.to);
    }
}