package com.mineinabyss.deeperworld.world

import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.deeperworld.world.section.AbstractSectionKey.CustomSectionKey
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.deeperworld.world.section.SectionKey
import org.bukkit.Location
import org.bukkit.World

class WorldManagerImpl : WorldManager {
    override val sections get() = sectionMap.values.toSet()

    private val sectionMap = deeperWorld.config.sections.associateBy { it.key }.toMutableMap()


    override fun registerSection(name: String, section: Section): SectionKey =
        registerInternal(CustomSectionKey(name), section)

    override fun registerSection(sectionKey: SectionKey, section: Section): SectionKey =
        registerInternal(sectionKey, section)

    private fun registerInternal(key: SectionKey, section: Section): SectionKey {
        if (key in sectionMap) throw RuntimeException("Bruh") //TODO change to checked exception
        sectionMap[key] = section
        return key
    }

    override fun unregisterSection(key: SectionKey) = TODO()

    override fun getSectionFor(location: Location): Section? {
        return getSectionFor(location.blockX, location.blockY, location.blockZ, location.world!!)
    }

    override fun getSectionFor(x: Int, y: Int, z: Int, world: World): Section? = //TODO consider performance
        sectionMap.values.firstOrNull { it.world == world && it.region.contains(x, y, z) }

    override fun getSectionFor(key: SectionKey) = sectionMap[key]
    override fun getSectionFor(key: String) = sectionMap[CustomSectionKey(key)]
}
