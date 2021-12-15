package com.mineinabyss.deeperworld.movement

interface TeleportHandler {
    fun handleTeleport()
    fun isValidTeleport() : Boolean
}
