package com.mineinabyss.deeperworld.sections

import com.mineinabyss.deeperworld.datastructures.KeyedSection
import com.mineinabyss.deeperworld.movement.transition.LocationSection
import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import com.mineinabyss.deeperworld.movement.transition.TransitionKind
import org.bukkit.Location
import org.bukkit.block.Block

class SectionRepository(
    private val sectionsDataSource: SectionDataSource,
) {
    val sections get() = sectionsDataSource.sections

    /** Gets a section by its [SectionKey], if it exists. */
    operator fun get(key: SectionKey): KeyedSection? = sectionsDataSource[key]

    /** Gets the section at the given location, or null if the location is not in a section. */
    operator fun get(location: Location): LocationSection? {
        val section = sectionsDataSource[location]
        return if (section != null) LocationSection(location, section) else null
    }

    /** Gets the depth in blocks of the given [location], taking sections into account. */
    fun getDepth(location: Location): Int? = sectionsDataSource.getDepth(location)

    fun inTransition(
        from: Location,
        to: Location,
    ): SectionTransition? {
        val fromSection = from.section ?: return null
        val toSection = to.section ?: return null
        val corrLoc = when {
            to.inSectionTransition -> to.correspondingLocation
            fromSection != toSection -> to
            else -> null
        } ?: return null
        val kind = if (to.y < from.y) TransitionKind.DESCEND else TransitionKind.ASCEND

        return corrLoc.section?.let {
            SectionTransition(from, corrLoc, fromSection.section.section, it.section.section, kind, fromSection != toSection)
        }
    }

    inline fun whenLinked(from: Location, runs: (linked: Location, transition: TransitionKind) -> Unit) {
        val result = get(from) ?: return
        val linked = result.linkedLocation ?: return
        val kind = result.kind ?: return
        runs(linked, kind)
    }

    inline fun whenLinked(from: Block, runs: (linked: Block) -> Unit) {
        whenLinked(from.location) { linked, _ -> runs(linked.block) }
    }

    /** Runs [runs] for each block in [from] that is in a section transition. */
    inline fun forEachLinked(from: List<Block>, runs: (block: Block, linked: Block) -> Unit) {
        from.forEach { block ->
            whenLinked(block) { linked -> runs(block, linked) }
        }
    }
}
