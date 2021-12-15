package com.mineinabyss.deeperworld.extensions

import org.bukkit.entity.Entity

internal fun Entity.getRootVehicle(): Entity? {
    var currentVehicle = vehicle

    while (currentVehicle?.vehicle != null) {
        currentVehicle = currentVehicle.vehicle
    }

    return currentVehicle
}

internal fun Entity.getPassengersRecursive(): List<Entity> {
    return mutableListOf<Entity>().let { passengerList ->
        passengerList.addAll(passengers)

        for (passenger: Entity in passengers) {
            passenger.getPassengersRecursive().let { passengerList.addAll(it) }
        }

        return@let passengerList
    }
}
