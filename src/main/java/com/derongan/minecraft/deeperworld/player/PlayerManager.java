package com.derongan.minecraft.deeperworld.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A small class used for handling temporary teleport prevention
 */
public class PlayerManager {
    private Map<UUID, Boolean> playerMap;

    public PlayerManager() {
        playerMap = new HashMap<>();
    }

    public boolean playerCanTeleport(Player player){
        return playerMap.getOrDefault(player.getUniqueId(), true);
    }

    public void setPlayerCanTeleport(Player player, boolean canTeleport){
        playerMap.put(player.getUniqueId(), canTeleport);
    }
}
