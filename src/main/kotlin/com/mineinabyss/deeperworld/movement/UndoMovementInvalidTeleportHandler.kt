package com.mineinabyss.deeperworld.movement

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class UndoMovementInvalidTeleportHandler(override val entity: Entity, from: Location, to: Location) :
    InvalidTeleportHandler(from, to) {
    override fun handleInvalidTeleport() {
        entity.teleport(from)
    }
}
