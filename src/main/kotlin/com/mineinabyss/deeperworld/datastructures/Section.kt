package com.mineinabyss.deeperworld.datastructures

import com.mineinabyss.deeperworld.sections.SectionKey
import org.bukkit.Location
import org.bukkit.World
import kotlin.math.max
import kotlin.math.min


data class Section(
    val key: SectionKey,
    val name: String,
    val region: Region,
    val referenceTop: Location,
    val referenceBottom: Location,
    val world: World,
    val index: Int,
) {
    var above: Section? = null
        internal set
    var below: Section? = null
        internal set
    var overlapWithAbove: Int = 0
        internal set

    val height: Int get() = region.max.y - region.min.y

    val center: Location
        get() = Location(
            world,
            region.center.x.toDouble(),
            region.center.y.toDouble(),
            region.center.z.toDouble()
        )

    // TODO simplify by just calculating and storing overlapWithAbove, then returning the calculated value
    /** Calculates the vertical overlap between two sections. */
    fun overlapWith(other: Section): Int? {
        if (!isAdjacentTo(other)) return null
        if (min(region.max.y, other.region.max.y) <= max(region.min.y, other.region.min.y)) return null
        // We decide which two points we are translating between.
        val (yA, yB) = when {
            isOnTopOf(other) -> referenceBottom.blockY to other.referenceTop.blockY
            else -> referenceTop.blockY to other.referenceBottom.blockY
        }

        return max(region.max.y, other.region.max.y) - max(yA, yB) +
                (min(yA, yB) - min(region.min.y, other.region.min.y))
    }

    /** @return whether this section is above [other]. */
    fun isOnTopOf(other: Section) = this == other.above

    /** @return Whether this section is above or below [other] */
    fun isAdjacentTo(other: Section) = this.isOnTopOf(other) || other.isOnTopOf(this)

}