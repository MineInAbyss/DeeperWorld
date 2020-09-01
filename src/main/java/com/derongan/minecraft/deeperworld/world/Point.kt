package com.derongan.minecraft.deeperworld.world;

import java.util.Objects;
import org.bukkit.Location;
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


    public Point(Location l) {
        this.x = l.getBlockX();
        this.z = l.getBlockZ();
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

    public Point plus(Point other){
        return  new Point(x+other.x, z+other.z);
    }

    public  Point minus(Point other){
        return  new Point(x-other.x, z-other.z);
    }

    public  Point div(float o){
        return new Point((int)(x/o), (int)(z/o));
    }

    public  Point div(int o){
        return new Point((x/o), (z/o));
    }

    public double length(){
        return Math.sqrt(x*x+z*z);
    }
}
