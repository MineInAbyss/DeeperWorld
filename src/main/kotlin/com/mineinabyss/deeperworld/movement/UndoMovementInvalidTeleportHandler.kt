package com.mineinabyss.deeperworld.movement

import org.bukkit.Location
import org.bukkit.entity.Player

class UndoMovementInvalidTeleportHandler(player: Player, from: Location, to: Location) :
    InvalidTeleportHandler(player, from, to) {
    override fun handleInvalidTeleport() {
        player.teleport(from)
    }
}
