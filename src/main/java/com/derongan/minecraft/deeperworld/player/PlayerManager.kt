package com.derongan.minecraft.deeperworld.player

import org.bukkit.entity.Player
import java.util.*

/**
 * A small class used for handling temporary teleport prevention
 */
object PlayerManager {
    private val playerMap = mutableMapOf<UUID, Boolean>()

    fun playerCanTeleport(player: Player): Boolean {
        return playerMap.getOrDefault(player.uniqueId, true)
    }

    fun setPlayerCanTeleport(player: Player, canTeleport: Boolean) {
        playerMap[player.uniqueId] = canTeleport
    }
}