package com.derongan.minecraft.deeperworld.listeners

import com.derongan.minecraft.deeperworld.world.section.section
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

object PlayerListener : Listener {

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val player = event.player;
        if (player.gameMode == GameMode.SURVIVAL) {
            if (event.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || event.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
                if (event.to?.section == null) {
                    event.isCancelled = true;
                }
            }

        }
    }
}