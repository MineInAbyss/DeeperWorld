package com.derongan.minecraft.deeperworld

import org.bukkit.entity.Entity

internal fun Entity.getVehicleRecursive() : Entity? {
    var currentVehicle = vehicle

    while(currentVehicle?.vehicle != null){
        currentVehicle = currentVehicle.vehicle
    }

    return currentVehicle
}