package com.derongan.minecraft.deeperworld.movement.transition

import org.bukkit.Location
import org.bukkit.entity.Player

interface TransitionChecker {

    fun checkForTransition(
        player: Player,
        from: Location,
        to: Location
    ): SectionTransition
}