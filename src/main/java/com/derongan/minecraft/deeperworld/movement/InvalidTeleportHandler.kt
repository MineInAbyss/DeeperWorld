package com.derongan.minecraft.deeperworld.movement

import com.mineinabyss.idofront.messaging.color
import org.bukkit.Location
import org.bukkit.entity.Player

abstract class InvalidTeleportHandler(val player: Player, val from: Location, val to: Location) :
    TeleportHandler {
    final override fun handleTeleport() {
        handleInvalidTeleport()
        player.sendMessage("&cThere is no where for you to teleport".color())
    }

    override fun isValidTeleport(): Boolean {
        return false
    }

    abstract fun handleInvalidTeleport();
}