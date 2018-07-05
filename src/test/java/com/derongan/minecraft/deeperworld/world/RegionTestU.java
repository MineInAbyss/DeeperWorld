package com.derongan.minecraft.deeperworld.world;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegionTestU {

    @Test
    public void contains() {
        Region region = new Region(-1,-1,10,10);

        assertTrue(region.contains(-1,-1));
        assertTrue(region.contains(10,10));
        assertTrue(region.contains(8,5));

        assertFalse(region.contains(-2,5));
        assertFalse(region.contains(11,5));
        assertFalse(region.contains(5,-2));
        assertFalse(region.contains(5,11));
    }
}