package com.mineinabyss.deeperworld.movement

import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.movement.transition.ConfigSectionChecker
import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import com.mineinabyss.deeperworld.movement.transition.TransitionKind
import com.mineinabyss.deeperworld.movement.transition.toEvent
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object MovementHandler {
    private val sectionCheckers = listOf(ConfigSectionChecker)

    val teleportCooldown = mutableSetOf<UUID>()
    fun handleMovement(entity: Entity, from: Location, to: Location) {
        if (sectionCheckers.any { it.inSection(entity) }) {
            sectionCheckers.firstNotNullOfOrNull { it.checkForTransition(entity, from, to) }?.let {
                with(getTeleportHandler(entity, it)) {
                    if (this.isValidTeleport() && entity is Player) it.toEvent(entity).call { this@with.handleTeleport() }
                    else this.handleTeleport()
                }
            }
        } else (entity as? Player)?.applyOutOfBoundsDamage()
    }

    //TODO abstract this away. Should instead do out of bounds action if out of bounds.
    private fun Player.applyOutOfBoundsDamage() {
        if (deeperWorld.config.damageOutsideSections > 0.0
            && location.world !in deeperWorld.config.damageExcludedWorlds
            && (gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE)
            && location.world in deeperWorld.config.worlds
        ) {
            damage(0.01) //give a damage effect
            health = (health - deeperWorld.config.damageOutsideSections / 10)
                .coerceIn(0.0, getAttribute(Attribute.MAX_HEALTH)?.value) //ignores armor
            showTitle(
                Title.title(
                    "You are not in a managed section".miniMsg().color(NamedTextColor.RED),
                    "You will take damage upon moving!".miniMsg().color(NamedTextColor.GRAY),
                    Title.Times.times(
                        0.seconds.toJavaDuration(),
                        1.seconds.toJavaDuration(),
                        0.5.seconds.toJavaDuration()
                    )
                )
            )
        }
    }

    private fun getTeleportHandler(entity: Entity, sectionTransition: SectionTransition): TeleportHandler {
        return when {
            sectionTransition.teleportUnnecessary || entity.uniqueId in teleportCooldown -> EmptyTeleportHandler
            entity is Player && entity.gameMode != GameMode.SPECTATOR && sectionTransition.to.block.isSolid -> when (sectionTransition.kind) {
                TransitionKind.ASCEND -> UndoMovementInvalidTeleportHandler(entity, sectionTransition)
                else -> BedrockBlockingInvalidTeleportHandler(entity, sectionTransition)
            }
            else -> TransitionTeleportHandler(entity.vehicle ?: entity, sectionTransition)
        }
    }
}
