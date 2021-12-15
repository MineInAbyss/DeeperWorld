package com.mineinabyss.deeperworld.movement.transition

import com.mineinabyss.deeperworld.world.section.correspondingLocation
import com.mineinabyss.deeperworld.world.section.inSectionTransition
import com.mineinabyss.deeperworld.world.section.section
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
        val fromSection = from.section ?: return null
        val toSection = to.section ?: return null
        val corrLoc = when {
            to.inSectionTransition -> to.correspondingLocation
            fromSection != toSection -> to
            else -> null
        } ?: return null

        return SectionTransition(
            from,
            corrLoc,
            fromSection,
            corrLoc.section!!, // If inSectionTransition, must be non-null
            if (to.y < from.y) TransitionKind.DESCEND else TransitionKind.ASCEND,
            teleportUnnecessary = fromSection != toSection
        )
    }
}
