package com.mineinabyss.deeperworld.movement.teleport

import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object UndoMovementInvalidTeleportHandler: TeleportHandler {
    override val isValid: Boolean = false

    override fun handleTeleport(entity: Entity, transition: SectionTransition) {
        entity.teleport(transition.from)
    }
}