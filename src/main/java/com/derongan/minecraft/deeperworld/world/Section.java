package com.derongan.minecraft.deeperworld.world;

import org.bukkit.Location;
import org.bukkit.World;

public interface Section {
    /**
     * Gets the reference location between this section and the one above it.
     *
     * This method and the section above's {@link Section#getReferenceLocationBottom()}
     * represent the same location in physical space.
     * @return The top reference point
     */
    Location getReferenceLocationTop();

    /**
     * Gets the reference location between this section and the one below it.
     *
     * This method and the section below's {@link Section#getReferenceLocationTop()} ()}
     * represent the same location in physical space.
     * @return The bottom reference point
     */
    Location getReferenceLocationBottom();

    /**
     * Gets the next section that is below this one, or null if none exists
     * @return The section below this one.
     */
    Section getSectionBelow();

    /**
     * Gets the previous section that is above this one, or null if none exists
     * @return The next section above this one.
     */
    Section getSectionAbove();


    /**
     * Gets the world this section is a part of
     * @return The world this section is part of
     */
    World getWorld();


    /**
     * Gets the region within which this section is active
     * @return The region this section is active within
     */
    Region getRegion();
}
