package com.derongan.minecraft.deeperworld.world

import kotlin.math.max
import kotlin.math.min

/**
 * Represents a region of the world. Contains all Y values.
 */
class Region(val a: Point, val b: Point) {
    constructor(ax: Int, az: Int, bx: Int, bz: Int) : this(Point(ax, az), Point(bx, bz))

    val center: Point get() = a.plus(b).div(2)

    fun contains(x: Int, z: Int): Boolean = x in min(a.x, b.x)..max(a.x, b.x) && z in min(a.z, b.z)..max(a.z, b.z)

    operator fun contains(p: Point) = contains(p.x, p.z)
}
