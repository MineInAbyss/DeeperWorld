package com.derongan.minecraft.deeperworld.listeners

import com.derongan.minecraft.deeperworld.services.canMoveSections
import com.derongan.minecraft.deeperworld.world.section.inSectionTransition
import com.derongan.minecraft.deeperworld.world.section.section
import com.mineinabyss.idofront.messaging.error
import org.bukkit.GameMode.CREATIVE
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.ENDER_PEARL

object PlayerListener : Listener {
    @EventHandler
    fun PlayerTeleportEvent.onPlayerTeleport() {
        if (player.gameMode == CREATIVE) return
        if (cause != ENDER_PEARL && cause != CHORUS_FRUIT) return
        if (!player.canMoveSections) return

        if (
            to.section == null ||
            to.section != player.location.section ||
            to.inSectionTransition
        ) {
            player.error("Teleportation is disabled between Layers and Sections.")
            isCancelled = true
        }
    }
}

//TODO move into idofront
private operator fun PlayerTeleportEvent.component4() = cause
