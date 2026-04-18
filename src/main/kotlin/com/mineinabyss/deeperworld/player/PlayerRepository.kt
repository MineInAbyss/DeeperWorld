package com.mineinabyss.deeperworld.player

import com.mineinabyss.deeperworld.deeperWorld
import org.bukkit.entity.Player

/**
 * A small class used for handling temporary teleport prevention
 */
interface PlayerRepository {
    fun canTeleport(player: Player): Boolean
    fun setCanTeleport(player: Player, canTeleport: Boolean)
}

var Player.canMoveSections: Boolean
    get() = deeperWorld.players.canTeleport(this)
    set(value) = deeperWorld.players.setCanTeleport(this, value)
