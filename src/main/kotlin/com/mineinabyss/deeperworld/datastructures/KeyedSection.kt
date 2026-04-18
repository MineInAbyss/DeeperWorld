package com.mineinabyss.deeperworld.datastructures

import com.mineinabyss.deeperworld.sections.SectionKey
import kotlin.math.max
import kotlin.math.min

data class KeyedSection(
    val key: SectionKey,
    val section: Section,
    val index: Int,
) {
    var above: KeyedSection? = null
        internal set
    var below: KeyedSection? = null
        internal set
    var overlapWithAbove: Int = 0
        internal set

    // TODO simplify by just calculating and storing overlapWithAbove, then returning the calculated value
    /** Calculates the vertical overlap between two sections. */
    fun overlapWith(other: KeyedSection): Int? {
        if (!isAdjacentTo(other)) return null
        if (min(section.region.max.y, other.section.region.max.y) <= max(section.region.min.y, other.section.region.min.y)) return null
        // We decide which two points we are translating between.
        val (yA, yB) = when {
            isOnTopOf(other) -> section.referenceBottom.blockY to other.section.referenceTop.blockY
            else -> section.referenceTop.blockY to other.section.referenceBottom.blockY
        }

        return max(section.region.max.y, other.section.region.max.y) - max(yA, yB) +
                (min(yA, yB) - min(section.region.min.y, other.section.region.min.y))
    }

    /** @return whether this section is above [other]. */
    fun isOnTopOf(other: KeyedSection) = this == other.above

    /** @return Whether this section is above or below [other] */
    fun isAdjacentTo(other: KeyedSection) = this.isOnTopOf(other) || other.isOnTopOf(this)

}