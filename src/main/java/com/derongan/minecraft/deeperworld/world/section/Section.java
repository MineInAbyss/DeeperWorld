package com.derongan.minecraft.deeperworld.world.section;

import com.derongan.minecraft.deeperworld.world.Region;
import com.derongan.minecraft.deeperworld.world.WorldManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;

public class Section {
    private Location referenceTop;
    private Location referenceBottom;

    private Region region;
    private World world;

    private SectionKey aboveKey;
    private SectionKey key;
    private SectionKey belowKey;

    private Section() {
    }

    /**
     * Gets the reference location between this section and the one above it.
     * <p>
     * This method and the section above's {@link Section#getReferenceLocationBottom()}
     * represent the same location in physical space.
     *
     * @return The top reference point
     */
    public Location getReferenceLocationTop() {
        return referenceTop;
    }

    /**
     * Gets the reference location between this section and the one below it.
     * <p>
     * This method and the section below's {@link Section#getReferenceLocationTop()} ()}
     * represent the same location in physical space.
     *
     * @return The bottom reference point
     */
    public Location getReferenceLocationBottom() {
        return referenceBottom;
    }

    /**
     * Gets the next section that is below this one, or null if none exists
     *
     * @return The key for the section below this one.
     */
    public SectionKey getKeyForSectionBelow() {
        return belowKey;
    }

    /**
     * Gets the previous section that is above this one, or null if none exists
     *
     * @return The key for the section above this one.
     */
    public SectionKey getKeyForSectionAbove() {
        return aboveKey;
    }

    /**
     * Gets the key for this section
     * @return The key for this section
     */
    public SectionKey getKey() {
        return key;
    }

    /**
     * Gets the world this section is a part of
     *
     * @return The world this section is part of
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the region within which this section is active
     *
     * @return The region this section is active within
     */
    public Region getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return key.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Location referenceTop;
        private Location referenceBottom;

        private Region region;
        private World world;

        private SectionKey aboveKey = SectionKey.TERMINAL;
        private SectionKey key;
        private SectionKey belowKey = SectionKey.TERMINAL;

        public Builder setReferenceTop(Location referenceTop) {
            this.referenceTop = referenceTop;
            return this;
        }

        public Builder setReferenceBottom(Location referenceBottom) {
            this.referenceBottom = referenceBottom;
            return this;
        }

        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder setWorld(World world) {
            this.world = world;
            return this;
        }

        public Builder setSectionAbove(SectionKey aboveKey) {
            this.aboveKey = aboveKey;
            return this;
        }

        public Builder setSectionBelow(SectionKey belowKey) {
            this.belowKey = belowKey;
            return this;
        }

        public Builder setSectionAbove(String aboveKey) {
            this.aboveKey = new AbstractSectionKey.CustomSectionKey(aboveKey);
            return this;
        }

        public Builder setSectionBelow(String belowKey) {
            this.aboveKey = new AbstractSectionKey.CustomSectionKey(belowKey);
            return this;
        }

        public Builder setName(SectionKey namekey) {
            this.key = namekey;
            return this;
        }

        public Builder setName(String name) {
            this.key = new AbstractSectionKey.CustomSectionKey(name);
            return this;
        }

        public Section build() {
            Section section = new Section();

            Validate.notNull(world);
            Validate.notNull(key);

            section.referenceBottom = referenceBottom;
            section.referenceTop = referenceTop;
            section.region = region;
            section.world = world;
            section.belowKey = belowKey;
            section.aboveKey = aboveKey;
            section.key = key;

            return section;
        }
    }
}
