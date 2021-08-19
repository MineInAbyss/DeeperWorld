package com.derongan.minecraft.deeperworld.movement.transition

import com.derongan.minecraft.deeperworld.world.section.correspondingLocation
import com.derongan.minecraft.deeperworld.world.section.section
import org.bukkit.Location
import org.bukkit.entity.Player

object ConfigTransitionChecker : TransitionChecker {
    override fun checkForTransition(
        player: Player,
        from: Location,
        to: Location
    ): SectionTransition? {
        return to.correspondingLocation?.let {
            SectionTransition(
                from,
                it,
                from.section,
                to.section,
                if (to.y < from.y) TransitionKind.DESCEND else TransitionKind.ASCEND
            )
        }

    }
}