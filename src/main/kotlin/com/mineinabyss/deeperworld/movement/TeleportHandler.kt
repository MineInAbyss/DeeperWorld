package com.mineinabyss.deeperworld.movement

import org.bukkit.entity.Entity

interface TeleportHandler {
    val entity: Entity
    fun handleTeleport()
    fun isValidTeleport() : Boolean
}
