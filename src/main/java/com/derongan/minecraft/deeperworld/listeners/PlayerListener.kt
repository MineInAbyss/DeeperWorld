package com.derongan.minecraft.deeperworld.listeners

import com.derongan.minecraft.deeperworld.services.canMoveSections
import com.derongan.minecraft.deeperworld.world.section.inSectionTransition
import com.derongan.minecraft.deeperworld.world.section.section
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import org.bukkit.GameMode.CREATIVE
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.ENDER_PEARL

object PlayerListener : Listener {
    @EventHandler
    fun PlayerTeleportEvent.onPlayerTeleport() {
        if (player.gameMode == CREATIVE) return player.info("Creative mode allowed")
        if (cause != ENDER_PEARL && cause != CHORUS_FRUIT) return player.info("non epearl tp allowed")
        if (!player.canMoveSections) return player.info("dw tp off")

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