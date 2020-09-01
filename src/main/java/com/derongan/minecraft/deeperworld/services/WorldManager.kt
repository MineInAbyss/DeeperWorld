package com.derongan.minecraft.deeperworld.services

import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.deeperworld.world.section.SectionKey
import com.mineinabyss.idofront.plugin.getService
import org.bukkit.Location
import org.bukkit.World

/**
 * Manages sections within the world.
 */
interface WorldManager {
    companion object : @JvmStatic WorldManager by getService()

    /**
     * Given a location, return the Section it is within
     *
     * @param location The location to check
     * @return The section or null if the location is not within a section.
     */
    fun getSectionFor(location: Location): Section?

    /**
     * Given x,y coords and a world, return the Section it is within
     *
     * @param x     The x coordinate
     * @param z     The z coordinate
     * @param world The world
     * @return The section or null if the location is not within a section.
     */
    fun getSectionFor(x: Int, z: Int, world: World): Section?

    /**
     * Gets the section associated with the provided key
     *
     * @param key The section key
     * @return The section associated or null of none exists
     */
    fun getSectionFor(key: SectionKey): Section?

    /**
     * Gets the section associated with the provided string
     *
     * @param key The section name
     * @return The section associated or null of none exists
     */
    fun getSectionFor(key: String): Section?

    /**
     * Register a section with a specific name
     *
     * @param name    The name for the section. Must be unique
     * @param section The section
     * @return a key used for retrieving this section
     */
    fun registerSection(name: String, section: Section): SectionKey

    /**
     * Register a section with an automatically computed name.
     *
     * @param sectionKey
     * @param section    The section
     * @return a key used for retrieving this section
     */
    fun registerSection(sectionKey: SectionKey, section: Section): SectionKey

    /**
     * Removes a registered section
     *
     * @param key The section key returned when registering this section
     */
    fun unregisterSection(key: SectionKey)

    /**
     * Gets an immutable copy of the currently loaded sections.
     */
    val sections: Set<Section>
}