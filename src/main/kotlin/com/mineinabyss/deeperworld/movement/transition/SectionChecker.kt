package com.mineinabyss.deeperworld.movement.transition

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

interface SectionChecker {

    fun inSection(entity: Entity) : Boolean

    fun checkForTransition(
        entity: Entity,
        from: Location,
        to: Location
    ): SectionTransition?
}
