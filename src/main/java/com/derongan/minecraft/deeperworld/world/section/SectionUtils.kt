@file:JvmMultifileClass
@file:JvmName("SectionUtils")

package com.derongan.minecraft.deeperworld.world.section

import com.derongan.minecraft.deeperworld.MinecraftConstants
import com.derongan.minecraft.deeperworld.world.WorldManager
import org.apache.commons.lang.Validate
import org.bukkit.Bukkit
import org.bukkit.Location
import kotlin.math.max
import kotlin.math.min

internal val worldManager: WorldManager = Bukkit.getServicesManager().load(WorldManager::class.java)!!

val Location.section: Section? get() = worldManager.getSectionFor(this)

/**
 * The corresponding section which overlaps with this location's section. Will be null if the section is not in an
 * overlap, even if there is a section above or below, since it's unclear which section becomes the corresponding one.
 */
val Location.correspondingSection: Section?
    get() {
        val section = this.section ?: return null
        val above: Section? = worldManager.getSectionFor(section.aboveKey)
        val below: Section? = worldManager.getSectionFor(section.belowKey)
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
        return this.getCorrespondingLocation(section ?: return null, correspondingSection ?: return null)
    }

/**
 * Get the location on another layer that is the same abyss space as the initial location.
 * The two sections must be next to eachother
 * @receiver The initial location
 * @param sectionA        the section the initial location is on
 * @param sectionB        the section we are translating the point to
 * @return A new location that corresponds to the original location
 */
fun Location.getCorrespondingLocation(sectionA: Section, sectionB: Section): Location {
    validateSectionsAdjacent(sectionA, sectionB)

    // We decide which two points we are translating between.
    val (fromSectionLoc, toSectionLoc) = when (sectionA.isOnTopOf(sectionB)) {
        true -> Pair(sectionA.referenceBottom!!, sectionB.referenceTop!!)
        false -> Pair(sectionA.referenceTop!!, sectionB.referenceBottom!!)
    }

    // fromX + n = toX
    // toX - fromX = n
    val delta = toSectionLoc.toVector().subtract(fromSectionLoc.toVector())
    val newLoc = clone().add(delta)
    newLoc.world = sectionB.world
    return newLoc
}

val Location.inSectionOverlap: Boolean
    get() {
        return sharedBetween(section ?: return false, correspondingSection ?: return false)
    }

fun Location.sharedBetween(section: Section, otherSection: Section): Boolean {
    val shared = getSharedBlocks(section, otherSection)
    return when {
        section.isOnTopOf(otherSection) -> blockY <= shared
        otherSection.isOnTopOf(section) -> blockY >= MinecraftConstants.WORLD_HEIGHT - shared
        else -> false
    }
}

fun getSharedBlocks(sectionA: Section, sectionB: Section): Int {
    validateSectionsAdjacent(sectionA, sectionB)
    // We decide which two points we are translating between.
    val (locA, locB) = when (sectionA.isOnTopOf(sectionB)) {
        true -> Pair(sectionA.referenceBottom!!, sectionB.referenceTop!!)
        false -> Pair(sectionA.referenceTop!!, sectionB.referenceBottom!!)
    }
    val yA = locA.blockY
    val yB = locB.blockY
    return MinecraftConstants.WORLD_HEIGHT - max(yA, yB) + min(yA, yB)
}

fun Section.isOnTopOf(other: Section) = key == other.aboveKey

fun validateSectionsAdjacent(sectionA: Section, sectionB: Section) =
        Validate.isTrue(sectionA.isOnTopOf(sectionB) || sectionB.isOnTopOf(sectionA), "Sections must be adjacent")