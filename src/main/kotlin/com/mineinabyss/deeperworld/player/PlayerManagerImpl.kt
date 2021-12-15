package com.mineinabyss.deeperworld.player

import com.mineinabyss.deeperworld.services.PlayerManager
import org.bukkit.entity.Player
import java.util.*

class PlayerManagerImpl : PlayerManager {
    private val playerMap = mutableMapOf<UUID, Boolean>()

    override fun playerCanTeleport(player: Player): Boolean =
        playerMap.getOrDefault(player.uniqueId, true)

    override fun setPlayerCanTeleport(player: Player, canTeleport: Boolean) {
        playerMap[player.uniqueId] = canTeleport
    }
}
