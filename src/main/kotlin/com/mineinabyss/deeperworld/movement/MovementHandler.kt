package com.mineinabyss.deeperworld.movement

import com.mineinabyss.deeperworld.movement.teleport.EmptyTeleportHandler
import com.mineinabyss.deeperworld.movement.teleport.TeleportHandler
import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import com.mineinabyss.deeperworld.movement.transition.TransitionKind
import com.mineinabyss.deeperworld.movement.transition.toEvent
import com.mineinabyss.deeperworld.sections.SectionRepository
import com.mineinabyss.deeperworld.sections.inSection
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class MovementHandler(
    private val sections: SectionRepository,
    private val onOutOfSection: OutOfSectionAction,
    private val teleportHandler: TeleportHandler,
    private val invalidAscentHandler: TeleportHandler,
    private val invalidDescentHandler: TeleportHandler,
) : AutoCloseable {
    fun handleMovement(entity: Entity, from: Location, to: Location) {
        if (!entity.location.inSection) {
            (entity as? Player)?.let { onOutOfSection.handleOutOfSection(it) }
            return
        }

        val inTransition = sections.inTransition(from, to) ?: return
        val handler = getTeleportHandler(entity, inTransition)
        var allow = true
        // Call event to allow other plugins to cancel transition for players
        if (handler.isValid && entity is Player) allow = inTransition.toEvent(entity).callEvent()
        if (!allow) return
        handler.handleTeleport(entity, inTransition)
    }

    private fun getTeleportHandler(entity: Entity, sectionTransition: SectionTransition): TeleportHandler = when {
        sectionTransition.teleportUnnecessary -> EmptyTeleportHandler

        entity is Player && entity.gameMode != GameMode.SPECTATOR && sectionTransition.to.block.isSolid -> when (sectionTransition.kind) {
            TransitionKind.ASCEND -> invalidAscentHandler
            TransitionKind.DESCEND -> invalidDescentHandler
        }

        else -> teleportHandler
    }

    override fun close() {
        invalidDescentHandler.close()
        invalidAscentHandler.close()
        teleportHandler.close()
    }
}
