package com.derongan.minecraft.deeperworld.listeners

import com.derongan.minecraft.deeperworld.world.section.section
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.ENDER_PEARL

object PlayerListener : Listener {
    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.player.gameMode == GameMode.SURVIVAL
                && (event.cause == ENDER_PEARL || event.cause == CHORUS_FRUIT)
                && event.to?.section == null) {
            event.isCancelled = true
        }
    }
}