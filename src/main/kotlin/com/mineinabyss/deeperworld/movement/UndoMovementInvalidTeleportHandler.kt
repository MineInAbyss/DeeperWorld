package com.mineinabyss.deeperworld.movement

import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class UndoMovementInvalidTeleportHandler(entity: Entity, from: Location, to: Location) : InvalidTeleportHandler(entity, from, to) {
    constructor(player: Player, transition: SectionTransition) : this(player, transition.from, transition.to)
    override fun handleInvalidTeleport() {
        entity.teleport(from)
    }
}
