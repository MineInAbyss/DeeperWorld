package com.mineinabyss.deeperworld.services

import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.deeperworld.world.section.SectionKey
import com.mineinabyss.idofront.plugin.Services
import org.bukkit.Location
import org.bukkit.World

/**
 * Manages sections within the world.
 */
interface WorldManager {
    companion object : WorldManager by Services.get()

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
     * @param y     The y coordinate
     * @param z     The z coordinate
     * @param world The world
     * @return The section or null if the location is not within a section.
     */
    fun getSectionFor(x: Int, y: Int, z: Int, world: World): Section?

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
     * Gets the depth in blocks of the given location, taking sections into account
     *
     * @param location The location
     * @return The depth of the given location in blocks, or null if location is not in a managed section
     */
    fun getDepthFor(location: Location): Int?

    /**
     * Gets the depth in blocks of the given position, taking sections into account
     * @param x     The x coordinate
     * @param y     The y coordinate
     * @param z     The z coordinate
     * @param world The world
     * @return The depth of the given position in blocks, or null if position is not in a managed section
     */
    fun getDepthFor(x: Double, y: Double, z: Double, world: World): Int?

    /**
     * Gets an immutable copy of the currently loaded sections.
     */
    val sections: Set<Section>
}
