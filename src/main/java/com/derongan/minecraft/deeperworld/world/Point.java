package com.derongan.minecraft.deeperworld.world;

import java.util.Objects;

/**
 * Represents a single X/Z column in a minecraft world.
 */
public class Point {
    private int x;
    private int z;

    public Point(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point that = (Point) o;
        return getX() == that.getX() &&
                getZ() == that.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getZ());
    }
}
