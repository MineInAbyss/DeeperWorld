package com.mineinabyss.deeperworld.player

import org.bukkit.entity.Player
import java.util.*

class PlayerRepositoryImpl : PlayerRepository {
    private val playerMap = mutableMapOf<UUID, Boolean>()

    override fun canTeleport(player: Player): Boolean =
        playerMap.getOrDefault(player.uniqueId, true)

    override fun setCanTeleport(player: Player, canTeleport: Boolean) {
        playerMap[player.uniqueId] = canTeleport
    }
}
