package com.mineinabyss.deeperworld.movement.transition

import com.mineinabyss.deeperworld.datastructures.Section
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerChangeSectionEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.operators.plus
import org.bukkit.Location
import org.bukkit.entity.Player

data class SectionTransition(
    val from: Location,
    val to: Location,
    val fromSection: Section,
    val toSection: Section,
    val kind: TransitionKind,
    val teleportUnnecessary: Boolean,
)

/**
 * A [location] inside of a [section] with helpers for getting adjacent sections.
 */
data class SectionLocation(
    val location: Location,
    val section: Section,
) {
    val linkedSection: Section?
        get() { // Inexpensive calculation, don't cache
            val aboveSection = section.above
            val belowSection = section.below

            return when {
                sharedBetween(section, aboveSection) -> aboveSection
                sharedBetween(section, belowSection) -> belowSection
                else -> null
            }
        }
    val inOverlap: Boolean get() = linkedSection != null
    val inTransition: Boolean get() = inTransition()
    val kind get() = linkedSection?.isOnTopOf(section)?.let { if (it) TransitionKind.ASCEND else TransitionKind.DESCEND }

    val linkedLocation: Location? get() = correspondingLocation()

    /**
     * Whether a location is in a shared boundary between [section] and [otherSection] sections.
     */
    private fun sharedBetween(section: Section, otherSection: Section?): Boolean {
        val otherSection = otherSection ?: return false
        val overlap = section.overlapWith(otherSection) ?: return false
        return when {
            section.isOnTopOf(otherSection) -> location.blockY <= section.region.min.y + overlap
            otherSection.isOnTopOf(section) -> location.blockY >= otherSection.region.max.y - overlap
            else -> false
        }
    }

    private fun inTransition(): Boolean {
        val otherSection = linkedSection ?: return false
        val overlap = section.overlapWith(otherSection) ?: return false
        return if (section.isOnTopOf(otherSection)) location.blockY <= section.region.min.y + .3 * overlap
        else location.blockY >= section.region.max.y - .3 * overlap
    }

    private fun correspondingLocation(): Location? {
        val corresponding = linkedSection ?: return null

        // We decide which two points we are translating between.
        val delta = when (section.isOnTopOf(corresponding)) {
            true -> corresponding.referenceTop - section.referenceBottom
            false -> corresponding.referenceBottom - section.referenceTop
        }.toVector()
        val newLoc = location.clone() + delta
        newLoc.world = corresponding.world
        return newLoc
    }
}

enum class TransitionKind {
    ASCEND,
    DESCEND
}

internal fun SectionTransition.toEvent(player: Player): PlayerChangeSectionEvent {
    return if (this.kind == TransitionKind.ASCEND) {
        PlayerAscendEvent(player, fromSection, toSection)
    } else {
        PlayerDescendEvent(player, fromSection, toSection)
    }
}
