package com.derongan.minecraft.deeperworld.world

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.world.section.AbstractSectionKey.CustomSectionKey
import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.deeperworld.world.section.SectionKey
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration

class WorldManagerImpl(config: FileConfiguration) : WorldManager {
    override val sections get() = sectionMap.values.toSet()

    private val sectionMap: MutableMap<SectionKey, Section> = HashMap()


    override fun registerSection(name: String, section: Section): SectionKey =
        registerInternal(CustomSectionKey(name), section)

    override fun registerSection(sectionKey: SectionKey, section: Section): SectionKey =
        registerInternal(sectionKey, section)

    private fun registerInternal(key: SectionKey, section: Section): SectionKey {
        if (sectionMap.containsKey(key)) throw RuntimeException("Bruh") //TODO change to checked exception
        sectionMap[key] = section
        return key
    }

    override fun unregisterSection(key: SectionKey) = TODO()

    override fun getSectionFor(location: Location): Section? {
        return getSectionFor(location.blockX, location.blockZ, location.world!!)
    }

    override fun getSectionFor(x: Int, z: Int, world: World): Section? = //TODO consider performance
        sectionMap.values.firstOrNull { it.world == world && it.region.contains(x, z) }

    override fun getSectionFor(key: SectionKey) = sectionMap[key]
    override fun getSectionFor(key: String) = sectionMap[CustomSectionKey(key)]
}