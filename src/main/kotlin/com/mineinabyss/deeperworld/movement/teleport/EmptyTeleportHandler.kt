package com.mineinabyss.deeperworld.movement.teleport

import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import org.bukkit.entity.Entity

object EmptyTeleportHandler : TeleportHandler {
    override val isValid: Boolean = true
    override fun handleTeleport(entity: Entity, transition: SectionTransition) = Unit
}