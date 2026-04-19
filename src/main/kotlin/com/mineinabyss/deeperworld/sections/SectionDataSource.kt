package com.mineinabyss.deeperworld.sections

import com.mineinabyss.deeperworld.datastructures.KeyedSection
import org.bukkit.Location

interface SectionDataSource {
    operator fun get(key: SectionKey): KeyedSection?

    operator fun get(location: Location): KeyedSection?

    fun getDepth(location: Location): Int?

    val sections: List<KeyedSection>
}
