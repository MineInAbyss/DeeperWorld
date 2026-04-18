package com.mineinabyss.deeperworld.movement.teleport

import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import org.bukkit.entity.Entity

sealed interface TeleportHandler: AutoCloseable {
    val isValid: Boolean
    fun handleTeleport(entity: Entity, transition: SectionTransition)

    override fun close() = Unit
}