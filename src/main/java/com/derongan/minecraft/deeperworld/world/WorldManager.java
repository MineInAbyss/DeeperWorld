package com.derongan.minecraft.deeperworld.world;

import com.derongan.minecraft.deeperworld.world.section.Section;
import com.derongan.minecraft.deeperworld.world.section.SectionKey;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;

/**
 * Manages sections within the world.
 */
public interface WorldManager {
    /**
     * Given a location, return the Section it is within
     *
     * @param location The location to check
     * @return The section or null if the location is not within a section.
     */
    Section getSectionFor(Location location);

    /**
     * Given x,y coords and a world, return the Section it is within
     *
     * @param x     The x coordinate
     * @param z     The z coordinate
     * @param world The world
     * @return The section or null if the location is not within a section.
     */
    Section getSectionFor(int x, int z, World world);

    /**
     * Gets the section associated with the provided key
     *
     * @param key The section key
     * @return The section associated or null of none exists
     */
    Section getSectionFor(SectionKey key);

    /**
     * Gets the section associated with the provided string
     *
     * @param key The section name
     * @return The section associated or null of none exists
     */
    Section getSectionFor(String key);

    /**
     * Register a section with a specific name
     *
     * @param name    The name for the section. Must be unique
     * @param section The section
     * @return a key used for retrieving this section
     */
    SectionKey registerSection(String name, Section section);

    /**
     * Register a section with an automatically computed name.
     *
     * @param sectionKey
     * @param section    The section
     * @return a key used for retrieving this section
     */
    SectionKey registerSection(SectionKey sectionKey, Section section);

    /**
     * Removes a registered section
     *
     * @param key The section key returned when registering this section
     */
    void unregisterSection(SectionKey key);

    /**
     * Gets an immutable copy of the currently loaded sections.
     */
    Collection<Section> getSections();
}
