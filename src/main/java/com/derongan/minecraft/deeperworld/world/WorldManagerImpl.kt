package com.derongan.minecraft.deeperworld.world;

import com.derongan.minecraft.deeperworld.world.section.AbstractSectionKey;
import com.derongan.minecraft.deeperworld.world.section.Section;
import com.derongan.minecraft.deeperworld.world.section.SectionKey;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class WorldManagerImpl implements WorldManager {
    public static final String SECTION_KEY = "sections";
    public static final String REF_TOP_KEY = "refTop";
    public static final String REF_BOTTOM_KEY = "refBottom";
    public static final String WORLD_KEY = "world";
    public static final String REGION_KEY = "region";
    public static final String NAME_KEY = "name";

    private Map<SectionKey, Section> sectionMap;

    public WorldManagerImpl(FileConfiguration config) {
        this.sectionMap = new HashMap<>();

        List<Map<?, ?>> sectionList = config.getMapList(SECTION_KEY);

        List<SectionKey> keys = sectionList.stream().map(this::getKey).collect(Collectors.toList());

        for (int i = 0; i < keys.size(); i++) {
            Map<?, ?> map = sectionList.get(i);

            String worldName = (String) map.get(WORLD_KEY);
            World world = Bukkit.getWorld(worldName);

            List<Integer> regionPoints = (List<Integer>) map.get(REGION_KEY);

            Region region = new Region(regionPoints.get(0), regionPoints.get(1), regionPoints.get(2), regionPoints.get(3));

            Location refBottom = parseLocation((List<Integer>) map.get(REF_BOTTOM_KEY), world);
            Location refTop = parseLocation((List<Integer>) map.get(REF_TOP_KEY), world);

            Section.Builder builder = Section.builder()
                    .setReferenceTop(refTop)
                    .setReferenceBottom(refBottom)
                    .setRegion(region)
                    .setWorld(world)
                    .setName(keys.get(i));

            if (i != 0)
                builder.setSectionAbove(keys.get(i - 1));
            if (i < keys.size() - 1)
                builder.setSectionBelow(keys.get(i + 1));

            registerSection(keys.get(i), builder.build());
        }
    }

    private SectionKey getKey(Map<?, ?> sectionData) {
        String name = (String) sectionData.get(NAME_KEY);
        if (name == null)
            return new AbstractSectionKey.InternalSectionKey();
        else
            return new AbstractSectionKey.CustomSectionKey(name);
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
    public SectionKey registerSection(String name, Section section) {
        return registerInternal(new AbstractSectionKey.CustomSectionKey(name), section);
    }

    @Override
    public SectionKey registerSection(SectionKey sectionKey, Section section) {
        return registerInternal(sectionKey, section);
    }

    private SectionKey registerInternal(SectionKey key, Section section) {
        if (sectionMap.containsKey(key))
            throw new RuntimeException("Bruh"); //TODO change to checked exception

        sectionMap.put(key, section);

        return key;
    }

    @Override
    public void unregisterSection(SectionKey key) {
        //TODO
    }

    @Override
    public Section getSectionFor(int x, int z, World world) {
        //TODO consider performance
        for (Section section : sectionMap.values()) {
            if (section.getWorld().equals(world) && section.getRegion().contains(x, z)) {
                return section;
            }
        }

        return null;
    }

    @Override
    public Section getSectionFor(SectionKey key) {
        return sectionMap.get(key);
    }

    @Override
    public Section getSectionFor(String key) {
        return getSectionFor(new AbstractSectionKey.CustomSectionKey(key));
    }

    @Override
    public Collection<Section> getSections() {
        return ImmutableList.copyOf(sectionMap.values());
    }

}
