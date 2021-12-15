package com.mineinabyss.deeperworld.world

/**
 * Represents a single X/Y/Z cube in a minecraft world.
 */
data class CubePoint(val x: Int, val y: Int, val z: Int) {
    operator fun plus(other: CubePoint) = CubePoint(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: CubePoint) = CubePoint(x - other.x, y - other.y, z - other.z)

    operator fun div(o: Float) = CubePoint((x / o).toInt(), (y / o).toInt(), (z / o).toInt())

    operator fun div(o: Int) = CubePoint(x / o, y / o, z / o)
}
