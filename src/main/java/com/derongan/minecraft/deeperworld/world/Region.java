package com.derongan.minecraft.deeperworld.world;

/**
 * Represents a region of the world. Contains all Y values.
 */
public class Region {
    private Point a;
    private Point b;

    public Region(int ax, int az, int bx, int bz) {
        a = new Point(ax,az);
        b = new Point(bx, bz);
    }

    public boolean contains(int x, int z) {
        return x <= Math.max(a.getX(), b.getX()) && x >= Math.min(a.getX(), b.getX()) && z <= Math.max(a.getZ(), b.getZ()) && z >= Math.min(a.getZ(), b.getZ());
    }


    public boolean contains(Point p) {
        return contains(p.getX(), p.getZ());
    }

    public Point getA() {
        return a;
    }

    public Point getB() {
        return b;
    }

    public Point midPoint(){
        return a.plus(b).div(2);
    }
}
