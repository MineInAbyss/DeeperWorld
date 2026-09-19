package com.mineinabyss.deeperworld.movement

import com.mineinabyss.deeperworld.Permissions
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.extensions.passengersRecursive
import com.mineinabyss.deeperworld.player.canMoveSections
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.vehicle.VehicleMoveEvent

class MovementListener(
    private val handler: MovementHandler,
) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun PlayerMoveEvent.move() {
        if (player.world !in deeperWorld.config.worlds) return
        if (!hasExplicitlyChangedBlock() || !player.hasPermission(Permissions.CHANGE_SECTION) || !player.canMoveSections) return
        handler.handleMovement(player, from, to)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun VehicleMoveEvent.move() {
        if (vehicle.world !in deeperWorld.config.worlds) return
        if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ) return
        val players = vehicle.passengersRecursive().filterIsInstance<Player>()

        val teleportEntity = players.firstOrNull { it.hasPermission(Permissions.CHANGE_SECTION) && it.canMoveSections } ?: vehicle
        handler.handleMovement(teleportEntity, from, to)
    }

    @EventHandler
    fun EntityMoveEvent.entityMove() {
        if (entity.world !in deeperWorld.config.worlds) return
        if (hasExplicitlyChangedBlock() && !entity.isLeashed) {
            handler.handleMovement(entity, from, to)
        }
    }
}
