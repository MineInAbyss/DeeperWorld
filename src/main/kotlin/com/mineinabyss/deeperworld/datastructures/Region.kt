package com.mineinabyss.deeperworld.datastructures

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a region of the world.
 */
@Serializable
data class Region(val start: CubePoint, val end: CubePoint) {
    @Transient
    val min = CubePoint(min(start.x, end.x), min(start.y, end.y), min(start.z, end.z))

    @Transient
    val max = CubePoint(max(start.x, end.x), max(start.y, end.y), max(start.z, end.z))

    constructor(ax: Int, ay: Int, az: Int, bx: Int, by: Int, bz: Int) : this(
        CubePoint(ax, ay, az),
        CubePoint(bx, by, bz)
    )

    val center: CubePoint get() = start.plus(end).div(2)

    fun contains(x: Int, y: Int, z: Int): Boolean {
        return x in min.x..max.x && y in min.y..max.y && z in min.z..max.z
    }

    operator fun contains(p: CubePoint) = contains(p.x, p.y, p.z)
}

