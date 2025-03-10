package com.mineinabyss.deeperworld.listeners

import com.mineinabyss.deeperworld.services.canMoveSections
import com.mineinabyss.deeperworld.world.section.inSectionTransition
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.idofront.location.up
import com.mineinabyss.idofront.messaging.error
import org.bukkit.GameMode.CREATIVE
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.ENDER_PEARL
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleExitEvent

object PlayerListener : Listener {
    @EventHandler
    fun PlayerTeleportEvent.onPlayerTeleport() {
        if (player.gameMode == CREATIVE || !player.canMoveSections) return
        if (cause != ENDER_PEARL && cause != CHORUS_FRUIT) return
        if (to.section != null && to.section == player.location.section && !to.inSectionTransition) return

        player.error("Teleportation is disabled between Layers and Sections.")
        isCancelled = true
    }

    @EventHandler
    fun VehicleEnterEvent.onEnterVehicle() {
        if (vehicle.location.up(1).section != null) return
        entered.error("The Abyss prevents you from mounting here...")
        isCancelled = true
    }

    @EventHandler
    fun VehicleExitEvent.onExitVehicle() {
        if (vehicle.location.up(1).section != null) return
        exited.error("The Abyss prevents you from dismounting here...")
        isCancelled = true
    }
}
