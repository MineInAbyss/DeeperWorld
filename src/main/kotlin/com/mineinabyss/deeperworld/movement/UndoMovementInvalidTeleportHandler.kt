package com.mineinabyss.deeperworld.movement

import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import org.bukkit.Location
import org.bukkit.entity.Player

class UndoMovementInvalidTeleportHandler(player: Player, from: Location, to: Location) : InvalidTeleportHandler(player, from, to) {
    constructor(player: Player, transition: SectionTransition) : this(player, transition.from, transition.to)
    override fun handleInvalidTeleport() {
        player.teleport(from)
    }
}
