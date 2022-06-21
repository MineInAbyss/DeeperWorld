package com.mineinabyss.deeperworld.movement

import com.mineinabyss.deeperworld.config.DeeperConfig
import com.mineinabyss.deeperworld.movement.transition.ConfigSectionChecker
import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import com.mineinabyss.deeperworld.movement.transition.TransitionKind
import com.mineinabyss.deeperworld.movement.transition.toEvent
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.miniMsg
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object MovementHandler {
    private val sectionCheckers = listOf(ConfigSectionChecker)

    fun handleMovement(player: Player, from: Location, to: Location) {
        if (sectionCheckers.any { it.inSection(player) }) {
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
            } ?: return
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
            player.showTitle(
                Title.title(
                    "<red>You are not in a managed section".miniMsg(),
                    "<gray>You will take damage upon moving!".miniMsg(),
                    Title.Times.times(
                        0.seconds.toJavaDuration(),
                        1.seconds.toJavaDuration(),
                        0.5.seconds.toJavaDuration()
                    )
                )
            )
        }
    }


    private fun getTeleportHandler(
        player: Player,
        sectionTransition: SectionTransition
    ): TeleportHandler {
        if (sectionTransition.teleportUnnecessary) return object : TeleportHandler {
            override fun handleTeleport() {}

            override fun isValidTeleport() = true
        }
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

        return TransitionTeleportHandler(player, sectionTransition.from, sectionTransition.to)
    }
}
