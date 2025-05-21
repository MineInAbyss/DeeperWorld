package com.mineinabyss.deeperworld.extensions

import org.bukkit.entity.Entity

internal fun Entity.getRootVehicle(): Entity? {
    var currentVehicle = vehicle ?: return null
    while (currentVehicle.isInsideVehicle) {
        currentVehicle = currentVehicle.vehicle!!
    }

    return currentVehicle
}

internal fun Entity.getPassengersRecursive(): List<Entity> {
    return passengers.plus(passengers.flatMap { it.getPassengersRecursive() })
}
