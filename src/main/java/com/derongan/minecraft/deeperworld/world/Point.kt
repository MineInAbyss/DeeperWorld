package com.derongan.minecraft.deeperworld.world

import kotlin.math.sqrt

/**
 * Represents a single X/Z column in a minecraft world.
 */
data class Point(val x: Int, val z: Int) {
    operator fun plus(other: Point) = Point(x + other.x, z + other.z)

    operator fun minus(other: Point) = Point(x - other.x, z - other.z)

    operator fun div(o: Float) = Point((x / o).toInt(), (z / o).toInt())

    operator fun div(o: Int) = Point(x / o, z / o)

    val length get() = sqrt((x * x + z * z).toDouble())
}