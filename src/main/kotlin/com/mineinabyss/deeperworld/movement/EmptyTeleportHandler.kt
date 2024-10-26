package com.mineinabyss.deeperworld.movement

object EmptyTeleportHandler : TeleportHandler {

    override fun handleTeleport() {}
    override fun isValidTeleport() = true
}