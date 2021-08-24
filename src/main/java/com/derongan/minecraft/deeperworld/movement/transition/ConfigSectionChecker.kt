package com.derongan.minecraft.deeperworld.movement.transition

import com.derongan.minecraft.deeperworld.world.section.correspondingLocation
import com.derongan.minecraft.deeperworld.world.section.inSectionTransition
import com.derongan.minecraft.deeperworld.world.section.section
import org.bukkit.Location
import org.bukkit.entity.Player

object ConfigSectionChecker : SectionChecker {
    override fun inSection(player: Player): Boolean {
        return player.location.section != null
    }

    override fun checkForTransition(
        player: Player,
        from: Location,
        to: Location
    ): SectionTransition? {
        return to.takeIf { to.inSectionTransition }?.correspondingLocation?.let {
            SectionTransition(
                from,
                it,
                from.section!!, // If inSectionTransition, must be non-null
                it.section!!,
                if (to.y < from.y) TransitionKind.DESCEND else TransitionKind.ASCEND
            )
        }

    }
}