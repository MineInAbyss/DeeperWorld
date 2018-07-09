package com.derongan.minecraft.deeperworld.world.section;

import com.derongan.minecraft.deeperworld.MinecraftConstants;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import static com.derongan.minecraft.deeperworld.MinecraftConstants.WORLD_HEIGHT;

public class SectionUtils {
    /**
     * Get the location on another layer that is the same abyss space as the initial location.
     * The two sections must be next to eachother
     *
     * @param sectionA        the section the initial location is on
     * @param sectionB        the section we are translating the point to
     * @param initialLocation The initial location
     * @return A new location that corresponds to the original location
     */
    public static Location getCorrespondingLocation(Section sectionA, Section sectionB, Location initialLocation) {
        validateSectionsAdjacent(sectionA, sectionB);

        Location fromSectionLoc;
        Location toSectionLoc;

        // We decide which two points we are translating between.
        if (isOnTopOf(sectionA, sectionB)) {
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

    public static boolean isSharedLocation(Section inSection, Section otherSection, Location location) {
        int shared = getSharedBlocks(inSection, otherSection);

        if (isOnTopOf(inSection, otherSection)) {
            return location.getBlockY() <= shared;
        } else if (isOnTopOf(otherSection, inSection)) {
            return location.getBlockY() >= MinecraftConstants.WORLD_HEIGHT - shared;
        }

        return false;
    }

    public static int getSharedBlocks(Section sectionA, Section sectionB) {
        validateSectionsAdjacent(sectionA, sectionB);
        Location locA;
        Location locB;

        // We decide which two points we are translating between.
        if (isOnTopOf(sectionA, sectionB)) {
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

    public static boolean isOnTopOf(Section section, Section other) {
        return section.getKey().equals(other.getKeyForSectionAbove());
    }

    private static void validateSectionsAdjacent(Section sectionA, Section sectionB) {
        Validate.isTrue(isOnTopOf(sectionA, sectionB) || isOnTopOf(sectionB, sectionA), "Sections must be adjacent");
    }
}
