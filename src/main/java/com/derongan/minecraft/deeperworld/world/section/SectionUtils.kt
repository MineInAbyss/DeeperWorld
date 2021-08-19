@file:JvmMultifileClass
@file:JvmName("SectionUtils")

package com.derongan.minecraft.deeperworld.world.section

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.operators.plus
import org.bukkit.Location
import kotlin.math.max
import kotlin.math.min


val Location.section: ConfigSection? get() = WorldManager.getSectionFor(this)

val SectionKey.section get() = WorldManager.getSectionFor(this)

/**
 * The corresponding section which overlaps with this location's section. Will be null if the section is not in an
 * overlap, even if there is a section above or below, since it's unclear which section becomes the corresponding one.
 */
val Location.correspondingSection: ConfigSection?
    get() {
        val section = this.section ?: return null
        val above: ConfigSection? = section.aboveKey.section
        val below: ConfigSection? = section.belowKey.section
        return when {
            above != null && sharedBetween(section, above) -> above
            below != null && sharedBetween(section, below) -> below
            else -> null
        }
    }

/**
 * The location as it would be in the [correspondingSection]. Will be null if the section is not in an overlap.
 */
val Location.correspondingLocation: Location?
    get() {
        return getCorrespondingLocation(section ?: return null, correspondingSection ?: return null)
    }

/**
 * Get the location on another layer that is the same abyss space as the initial location.
 * The two sections must be next to eachother
 * @receiver The initial location
 * @param sectionA        the section the initial location is on
 * @param sectionB        the section we are translating the point to
 * @return A new location that corresponds to the original location
 */
fun Location.getCorrespondingLocation(sectionA: ConfigSection, sectionB: ConfigSection): Location? {
    if (!sectionA.isAdjacentTo(sectionB)) return null

    // We decide which two points we are translating between.
    val (fromSectionLoc, toSectionLoc) = when (sectionA.isOnTopOf(sectionB)) {
        true -> sectionA.referenceBottom to sectionB.referenceTop
        false -> sectionA.referenceTop to sectionB.referenceBottom
    }

    // fromX + n = toX
    // toX - fromX = n
    val delta = toSectionLoc.toVector() - (fromSectionLoc.toVector())
    val newLoc = clone() + delta
    newLoc.world = sectionB.world
    return newLoc
}

val Location.inSectionOverlap: Boolean
    get() {
        return sharedBetween(section ?: return false, correspondingSection ?: return false)
    }

val Location.inSectionTransition: Boolean
    get() {
        // Get overlap of this section and corresponding section
        val shared = section?.overlapWith(correspondingSection ?: return false) ?: return false
        return blockY >= world.maxHeight - .3 * shared || blockY <= world.minHeight + .3 * shared
    }

fun Location.sharedBetween(section: ConfigSection, otherSection: ConfigSection): Boolean {
    val overlap = section.overlapWith(otherSection) ?: return false
    return when {
        section.isOnTopOf(otherSection) -> blockY <= world.minHeight + overlap
        otherSection.isOnTopOf(section) -> blockY >= world.maxHeight - overlap
        else -> false
    }
}

fun ConfigSection.overlapWith(other: ConfigSection): Int? {
    if (!isAdjacentTo(other)) return null
    // We decide which two points we are translating between.
    val (locA, locB) = when (isOnTopOf(other)) {
        true -> referenceBottom to other.referenceTop
        false -> referenceTop to other.referenceBottom
    }
    val yA = locA.blockY
    val yB = locB.blockY
    return (world.maxHeight - max(yA, yB)) + (min(yA, yB) - world.minHeight)
}

fun ConfigSection.isOnTopOf(other: ConfigSection) = key == other.aboveKey

/** Whether a section is adjacent to another */
fun ConfigSection.isAdjacentTo(other: ConfigSection) = this.isOnTopOf(other) || other.isOnTopOf(this)
