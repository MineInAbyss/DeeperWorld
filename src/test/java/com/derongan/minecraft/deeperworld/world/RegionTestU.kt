package com.derongan.minecraft.deeperworld.world

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegionTestU {
    @Test
    fun contains() {
        val region = Region(-1, -1, 10, 10)
        assertTrue(region.contains(-1, -1))
        assertTrue(region.contains(10, 10))
        assertTrue(region.contains(8, 5))
        assertFalse(region.contains(-2, 5))
        assertFalse(region.contains(11, 5))
        assertFalse(region.contains(5, -2))
        assertFalse(region.contains(5, 11))
    }
}
