@file:JvmMultifileClass
@file:JvmName("SectionUtils")

package com.mineinabyss.deeperworld.world.section

import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.operators.plus
import org.bukkit.Location
import kotlin.math.max
import kotlin.math.min


val Location.section: Section? get() = WorldManager.getSectionFor(this)

val SectionKey.section get() = WorldManager.getSectionFor(this)

/**
 * The corresponding section which overlaps with this location's section. Will be null if the section is not in an
 * overlap, even if there is a section above or below, since it's unclear which section becomes the corresponding one.
 */
val Location.correspondingSection: Section?
    get() {
        val section = this.section ?: return null
        val aboveSection = section.aboveKey.section
        val belowSection = section.belowKey.section

        return when {
            sharedBetween(section, aboveSection) -> aboveSection
            sharedBetween(section, belowSection) -> belowSection
            else -> null
        }
    }

/**
 * The location as it would be in the [correspondingSection]. Will be null if the section is not in an overlap.
 */
val Location.correspondingLocation: Location?
    get() {
        return correspondingLocation(section ?: return null, correspondingSection ?: return null)
    }

/**
 * Get the location on another layer that is the same abyss space as the initial location.
 * The two sections must be next to eachother
 * @receiver The initial location
 * @param sectionA        the section the initial location is on
 * @param sectionB        the section we are translating the point to
 * @return A new location that corresponds to the original location
 */
fun Location.correspondingLocation(sectionA: Section, sectionB: Section): Location? {
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
        // Archive Server hardcoding for now
        val minHeight = if ("2021" in world.name) 0 else world.minHeight
        return blockY >= world.maxHeight - .3 * shared || blockY <= minHeight + .3 * shared
    }

fun Location.sharedBetween(section: Section, otherSection: Section?): Boolean {
    val overlap = section.overlapWith(otherSection ?: return false) ?: return false
    // Archive Server hardcoding for now
    val minHeight = if ("2021" in world.name) 0 else world.minHeight
    return when {
        section.isOnTopOf(otherSection) -> blockY <= minHeight + overlap
        otherSection.isOnTopOf(section) -> blockY >= world.maxHeight - overlap
        else -> false
    }
}

fun Section.overlapWith(other: Section): Int? {
    if (!isAdjacentTo(other)) return null
    if (min(this.region.max.y, other.region.max.y) <= max(this.region.min.y, other.region.min.y)) return null
    // We decide which two points we are translating between.
    val (yA, yB) = when {
        isOnTopOf(other) -> referenceBottom.blockY to other.referenceTop.blockY
        else -> referenceTop.blockY to other.referenceBottom.blockY
    }

    return max(this.region.max.y, other.region.max.y) - max(yA, yB) +
            (min(yA, yB) - min(this.region.min.y, other.region.min.y))
}

fun Section.isOnTopOf(other: Section) = key == other.aboveKey

/** Whether a section is adjacent to another */
fun Section.isAdjacentTo(other: Section) = this.isOnTopOf(other) || other.isOnTopOf(this)

val Section.centerLocation: Location
    get() = Location(world, region.center.x.toDouble(), region.center.y.toDouble(), region.center.z.toDouble())
