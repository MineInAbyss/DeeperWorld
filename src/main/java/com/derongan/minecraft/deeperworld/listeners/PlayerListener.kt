package com.derongan.minecraft.deeperworld.listeners

import com.derongan.minecraft.deeperworld.services.canMoveSections
import com.derongan.minecraft.deeperworld.world.section.section
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.idofront.messaging.error
import org.bukkit.GameMode.ADVENTURE
import org.bukkit.GameMode.SURVIVAL
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.ENDER_PEARL

object PlayerListener : Listener {
    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val (player, _, to, cause) = event
        if (
            (player.gameMode == SURVIVAL || player.gameMode == ADVENTURE)
            && (cause == ENDER_PEARL || cause == CHORUS_FRUIT)
            && player.canMoveSections
            && (to.section != player.location.section ||
                to.section == null ||
                to.blockY >= player.location.world.maxHeight - 5 ||
                to.blockY <= player.location.world.minHeight + 5)
        ) {
            player.error("Teleportation is disabled between Layers and Sections.")
            event.isCancelled = true
        }
    }
}

//TODO move into idofront
private operator fun PlayerTeleportEvent.component4() = cause