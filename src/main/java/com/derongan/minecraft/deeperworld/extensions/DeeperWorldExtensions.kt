package com.derongan.minecraft.deeperworld.extensions

import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

internal fun Entity.getVehicleRecursive(): Entity? {
    var currentVehicle = vehicle

    while (currentVehicle?.vehicle != null) {
        currentVehicle = currentVehicle.vehicle
    }

    return currentVehicle
}

internal fun Entity.getPlayerPassengersRecursive(): List<Player>? {
    return mutableListOf<Player>().let { playerList ->
        if (passengers.size != 0) {
            if (passengers.any { it is Player }) {
                playerList.addAll(passengers.filterIsInstance<Player>())
            }

            for (passenger: Entity in passengers) {
                passenger.getPlayerPassengersRecursive()?.let { playerList.addAll(it) }
            }
        }

        if (playerList.isNotEmpty()) {
            return@let playerList
        } else {
            return@let null
        }
    }
}

internal fun Player.getLeashedEntities(): List<LivingEntity>? {
    return getNearbyEntities(20.0, 20.0, 20.0)
            .filter { it is LivingEntity && (it.isLeashed && it.leashHolder == this) }
            .map { it as LivingEntity }
            .ifEmpty { null }
}

internal fun Entity.getNearbyItemEntities(v: Double, v1: Double, v2: Double, mat: Material? = null): List<Item>? {
    return getNearbyEntities(v, v1, v2).filterIsInstance<Item>().let { items ->
        when {
            mat != null -> {
                return@let items.filter { it.itemStack.type == mat }
            }
            items.isNotEmpty() -> {
                return@let items
            }
            else -> {
                return@let null
            }
        }
    }
}