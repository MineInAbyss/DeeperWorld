package com.derongan.minecraft.deeperworld.world;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WorldManagerImpl implements WorldManager {
    public static final String SECTION_KEY = "sections";
    public static final String REF_TOP_KEY = "refTop";
    public static final String REF_BOTTOM_KEY = "refBottom";
    public static final String WORLD_KEY = "world";
    public static final String REGION_KEY = "region";

    private List<Section> sectionList;

    public WorldManagerImpl(FileConfiguration config) {
        this.sectionList = new ArrayList<>();

        List<Map<?, ?>> layerlist = config.getMapList(SECTION_KEY);

        SectionImpl last = null;
        for (Map<?, ?> map : layerlist) {
            String worldName = (String) map.get(WORLD_KEY);
            World world = Bukkit.getWorld(worldName);

            List<Integer> regionPoints = (List<Integer>) map.get(REGION_KEY);

            Region region = new Region(regionPoints.get(0), regionPoints.get(1), regionPoints.get(2), regionPoints.get(3));

            Location refBottom = parseLocation((List<Integer>) map.get(REF_BOTTOM_KEY), world);
            Location refTop = parseLocation((List<Integer>) map.get(REF_TOP_KEY), world);

            SectionImpl section = new SectionImpl(refTop, refBottom, region, world);
            section.setSectionAbove(last);

            if (last != null) {
                last.setSectionBelow(section);
            }

            last = section;

            sectionList.add(section);
        }
    }

    private Location parseLocation(List<Integer> points, World world) {
        return new Location(world, points.get(0), points.get(1), points.get(2));
    }

    @Override
    public Section getSectionFor(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();

        return getSectionFor(x, z, location.getWorld());
    }

    @Override
    public Section getSectionFor(int x, int z, World world) {
        //TODO consider performance
        for (Section section : sectionList) {
            if (section.getWorld().equals(world) && section.getRegion().contains(x, z)) {
                return section;
            }
        }

        return null;
    }

    @Override
    public Collection<Section> getSections() {
        return ImmutableList.copyOf(sectionList);
    }
}
