package com.derongan.minecraft.deeperworld.world;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;

public interface WorldManager {
    Section getSectionFor(Location location);
    Section getSectionFor(int x, int z, World world);

    /**
     * Gets an immutable copy of the currently loaded sections.
     */
    Collection<Section> getSections();
}
