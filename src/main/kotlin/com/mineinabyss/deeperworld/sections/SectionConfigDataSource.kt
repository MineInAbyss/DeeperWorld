package com.mineinabyss.deeperworld.sections

import com.mineinabyss.deeperworld.DeeperWorldConfig
import com.mineinabyss.deeperworld.datastructures.Section
import org.bukkit.Location
import org.bukkit.World
import kotlin.math.floor

class SectionConfigDataSource(
    config: DeeperWorldConfig,
) : SectionDataSource {
    override val sections: List<Section> = config.sections.mapIndexed { index, section ->
        val world = section.world
        Section(
            key = section.name,
            name = section.name,
            region = section.region,
            referenceTop = section.refTop.toLocation(world),
            referenceBottom = section.refBottom.toLocation(world),
            world = world,
            index = index
        )
    }

    init {
        require(sections.distinctBy { it.key }.size == sections.size) { "Section keys must be unique" }
        // Populate above/below fields
        sections.zipWithNext { current, next ->
            current.below = next
            next.above = current
            next.overlapWithAbove = current.overlapWith(next) ?: 0
        }
    }

    private val byKey: Map<SectionKey, Section> = sections.associateBy { it.key }

    private val byWorld: Map<World, List<Section>> = sections.groupBy { it.world }

    override fun getDepth(location: Location): Int? {
        val lastSection = get(location) ?: return null
        var sum: Int = lastSection.region.max.y - floor(location.y).toInt() // Depth within current section
        for (i in 0..<lastSection.index) {
            val section = sections[i]
            sum += section.height - section.overlapWithAbove
        }
        return sum
    }

    override operator fun get(key: SectionKey) = byKey[key]

    override fun get(location: Location): Section? {
        val sectionsInWorld = byWorld[location.world] ?: return null
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ
        return sectionsInWorld.firstOrNull { it.region.contains(x, y, z) }
    }
}