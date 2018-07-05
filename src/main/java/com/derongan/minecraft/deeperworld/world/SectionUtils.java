package com.derongan.minecraft.deeperworld.world;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import static com.derongan.minecraft.deeperworld.MinecraftConstants.WORLD_HEIGHT;

public class SectionUtils {
    /**
     * Get the location on another layer that is the same abyss space as the initial location.
     * The two sections must be next to eachother
     * @param sectionA the section the initial location is on
     * @param sectionB the section we are translating the point to
     * @param initialLocation The initial location
     * @return A new location that corresponds to the original location
     */
    public static Location getCorrespondingLocation(Section sectionA, Section sectionB, Location initialLocation){
        validateSectionsAdjacent(sectionA, sectionB);

        Location fromSectionLoc;
        Location toSectionLoc;

        // We decide which two points we are translating between.
        if(sectionA.equals(sectionB.getSectionAbove())){
            fromSectionLoc = sectionA.getReferenceLocationBottom();
            toSectionLoc = sectionB.getReferenceLocationTop();
        } else {
            fromSectionLoc = sectionA.getReferenceLocationTop();
            toSectionLoc = sectionB.getReferenceLocationBottom();
        }

        // fromX + n = toX
        // toX - fromX = n
        Vector delta = toSectionLoc.toVector().subtract(fromSectionLoc.toVector());

        Location newLoc = initialLocation.clone().add(delta);
        newLoc.setWorld(sectionB.getWorld());
        return newLoc;
    }

    public static int getSharedBlocks(Section sectionA, Section sectionB){
        validateSectionsAdjacent(sectionA, sectionB);
        Location locA;
        Location locB;

        // We decide which two points we are translating between.
        if(sectionA.equals(sectionB.getSectionAbove())){
            locA = sectionA.getReferenceLocationBottom();
            locB = sectionB.getReferenceLocationTop();
        } else {
            locA = sectionA.getReferenceLocationTop();
            locB = sectionB.getReferenceLocationBottom();
        }

        int yA = locA.getBlockY();
        int yB = locB.getBlockY();

        return WORLD_HEIGHT - Math.max(yA, yB) + Math.min(yA, yB);
    }

    private static void validateSectionsAdjacent(Section sectionA, Section sectionB) {
        Validate.isTrue(sectionA.equals(sectionB.getSectionAbove()) || sectionA.equals(sectionB.getSectionBelow()), "Sections must be adjacent");
    }
}
