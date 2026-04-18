package com.mineinabyss.deeperworld.movement.transition

import com.mineinabyss.deeperworld.datastructures.KeyedSection
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerChangeSectionEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.deeperworld.datastructures.Section
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

data class LocationSection(
    val location: Location,
    val section: KeyedSection,
) {
    val linkedSection: KeyedSection? get() { // Inexpensive calculation, don't cache
        val aboveSection = section.above
        val belowSection = section.below

        return when {
            sharedBetween(section, aboveSection) -> aboveSection
            sharedBetween(section, belowSection) -> belowSection
            else -> null
        }
    }
    val inOverlap: Boolean get() =  linkedSection != null
    val inTransition: Boolean get() = sharedBetween(section, linkedSection, multiplier = 0.3)
    val kind get() = linkedSection?.isOnTopOf(section)?.let { if(it) TransitionKind.DESCEND else TransitionKind.ASCEND }

    val linkedLocation: Location? by lazy { correspondingLocation() }

    /**
     * Whether a location is in a shared boundary between [section] and [otherSection] sections.
     */
    //TODO avoid floating point errors when 1.0
    private fun sharedBetween(section: KeyedSection, otherSection: KeyedSection?, multiplier: Double = 1.0): Boolean {
        val otherSection = otherSection ?: return false
        val overlap = section.overlapWith(otherSection) ?: return false
        return when {
            section.isOnTopOf(otherSection) -> location.blockY <= section.section.region.min.y + multiplier * overlap
            otherSection.isOnTopOf(section) -> location.blockY >= otherSection.section.region.max.y - multiplier * overlap
            else -> false
        }
    }

    private fun correspondingLocation(): Location? {
        val corresponding = linkedSection ?: return null

        // We decide which two points we are translating between.
        val delta = when (section.isOnTopOf(corresponding)) {
            true -> corresponding.section.referenceTop - section.section.referenceBottom
            false -> section.section.referenceTop - corresponding.section.referenceBottom
        }.toVector()
        val newLoc = location.clone() + delta
        newLoc.world = corresponding.section.world
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
