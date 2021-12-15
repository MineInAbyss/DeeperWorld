package com.mineinabyss.deeperworld.listeners

import com.mineinabyss.deeperworld.Permissions
import com.mineinabyss.deeperworld.extensions.getPassengersRecursive
import com.mineinabyss.deeperworld.movement.MovementHandler
import com.mineinabyss.deeperworld.services.canMoveSections
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.vehicle.VehicleMoveEvent

object MovementListener : Listener {
    val temporaryBedrock = mutableListOf<Block>()

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun PlayerMoveEvent.move() {
        if (player.hasPermission(Permissions.CHANGE_SECTION_PERMISSION) && player.canMoveSections) {
            MovementHandler.handleMovement(player, from, to)
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun VehicleMoveEvent.move() {
        val players = vehicle.getPassengersRecursive().filterIsInstance<Player>()

        players.firstOrNull { it.hasPermission(Permissions.CHANGE_SECTION_PERMISSION) && it.canMoveSections }?.let {
            MovementHandler.handleMovement(it, from, to)
        }
    }

    @EventHandler
    fun EntityMoveEvent.entityMove() {
        if (entity.getPassengersRecursive().isEmpty()) return
        entity.getPassengersRecursive().filterIsInstance<Player>()
            .filter { rider -> rider.hasPermission(Permissions.CHANGE_SECTION_PERMISSION) && rider.canMoveSections }
            .forEach { MovementHandler.handleMovement(it, from, to) }
    }
}

