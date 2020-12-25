package com.derongan.minecraft.deeperworld

import org.bukkit.entity.Entity

internal tailrec fun Entity.getVehicleRecursive() : Entity? {
    if(!passengers.any() && vehicle == null){
        return null
    }

    val currentVehicle = vehicle ?: return this

    return currentVehicle.getVehicleRecursive()
}