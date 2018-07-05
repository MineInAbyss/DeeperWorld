package com.derongan.minecraft.deeperworld.world;

import org.bukkit.Location;
import org.bukkit.World;

public class SectionImpl implements Section {
    private Location referenceTop;
    private Location referenceBottom;

    private Section sectionBelow;
    private Section sectionAbove;

    private Region region;
    private World world;

    public SectionImpl(Location referenceTop, Location referenceBottom, Region region, World world) {
        this.referenceTop = referenceTop;
        this.referenceBottom = referenceBottom;
        this.region = region;
        this.world = world;
    }

    @Override
    public Location getReferenceLocationTop() {
        return referenceTop;
    }

    @Override
    public Location getReferenceLocationBottom() {
        return referenceBottom;
    }

    @Override
    public Section getSectionBelow() {
        return sectionBelow;
    }

    @Override
    public Section getSectionAbove() {
        return sectionAbove;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    public SectionImpl setSectionBelow(Section sectionBelow) {
        this.sectionBelow = sectionBelow;
        return this;
    }

    public SectionImpl setSectionAbove(Section sectionAbove) {
        this.sectionAbove = sectionAbove;
        return this;
    }
}
