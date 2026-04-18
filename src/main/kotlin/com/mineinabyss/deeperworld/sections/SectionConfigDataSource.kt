package com.mineinabyss.deeperworld.sections

import com.mineinabyss.deeperworld.DeeperWorldConfig
import com.mineinabyss.deeperworld.datastructures.KeyedSection
import org.bukkit.Location
import kotlin.math.floor

class SectionConfigDataSource(
    config: DeeperWorldConfig,
) : SectionDataSource {
    override val sections: List<KeyedSection> = config.sections.mapIndexed { index, section ->
        KeyedSection(
            key = "section-${index}-${section.name}",
            section = section,
            index = index
        )
    }

    init {
        // Populate above/below fields
        sections.zipWithNext { current, next ->
            current.below = next
            next.above = current
            next.overlapWithAbove = current.overlapWith(next) ?: 0
        }
    }

    // Array for faster iteration
    private val sectionsArray = config.sections.toTypedArray()

    private val byKey: Map<SectionKey, KeyedSection> = sections.associateBy { it.key }

    override fun getDepth(location: Location): Int? {
        val lastSection = get(location) ?: return null
        var sum: Int = lastSection.section.region.max.y - floor(location.y).toInt() + // Depth within current section
                lastSection.index // Each section after the first adds height + 1 to sum
        for (i in 0..<lastSection.index) {
            val section = sections[i]
            sum += section.section.height - section.overlapWithAbove
        }
        return sum
    }

    override operator fun get(key: SectionKey) = byKey[key]

    //TODO consider performance, currently just optimizing iteration overhead via array
    override fun get(location: Location): KeyedSection? {
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ
        val world = location.world
        for (i in sectionsArray.indices) {
            val section = sectionsArray[i]
            if (section.world == world && section.region.contains(x, y, z)) return sections[i]
        }
        return null
    }
}