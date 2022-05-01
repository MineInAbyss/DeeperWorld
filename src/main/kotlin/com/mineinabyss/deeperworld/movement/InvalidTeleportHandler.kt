package com.mineinabyss.deeperworld.movement

import org.bukkit.Location
import org.bukkit.entity.Player

abstract class InvalidTeleportHandler(val player: Player, val from: Location, val to: Location) :
    TeleportHandler {
    final override fun handleTeleport() {
        handleInvalidTeleport()
        player.sendMessage("<red>There is no where for you to teleport")
    }

    override fun isValidTeleport(): Boolean {
        return false
    }

    abstract fun handleInvalidTeleport();
}
