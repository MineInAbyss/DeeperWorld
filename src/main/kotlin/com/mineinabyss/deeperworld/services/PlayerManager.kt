package com.mineinabyss.deeperworld.services

import com.mineinabyss.idofront.plugin.getService
import org.bukkit.entity.Player

/**
 * A small class used for handling temporary teleport prevention
 */
interface PlayerManager {
    companion object : PlayerManager by getService()

    fun playerCanTeleport(player: Player): Boolean
    fun setPlayerCanTeleport(player: Player, canTeleport: Boolean)
}

var Player.canMoveSections
    get() = PlayerManager.playerCanTeleport(this)
    set(value) = PlayerManager.setPlayerCanTeleport(this, value)
