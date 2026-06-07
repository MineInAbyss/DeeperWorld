package com.mineinabyss.deeperworld.sections

import com.mineinabyss.deeperworld.datastructures.Section
import org.bukkit.Location

interface SectionDataSource {
    operator fun get(key: SectionKey): Section?

    operator fun get(location: Location): Section?

    fun getDepth(location: Location): Int?

    val sections: List<Section>
}
