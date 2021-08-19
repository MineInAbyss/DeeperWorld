package com.derongan.minecraft.deeperworld.movement.transition

import org.bukkit.Location
import org.bukkit.entity.Player

interface SectionChecker {

    fun inSection(player: Player) : Boolean

    fun checkForTransition(
        player: Player,
        from: Location,
        to: Location
    ): SectionTransition?
}