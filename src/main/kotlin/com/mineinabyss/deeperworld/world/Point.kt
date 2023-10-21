package com.mineinabyss.deeperworld.world

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

/**
 * Represents a single X/Y/Z cube in a minecraft world.
 */
@Serializable
data class CubePoint(val x: Int, val y: Int, val z: Int) {
    operator fun plus(other: CubePoint) = CubePoint(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: CubePoint) = CubePoint(x - other.x, y - other.y, z - other.z)

    operator fun div(o: Float) = CubePoint((x / o).toInt(), (y / o).toInt(), (z / o).toInt())

    operator fun div(o: Int) = CubePoint(x / o, y / o, z / o)

    override fun toString() = "$x,$y,$z"

    fun toLocation(world: World) = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
}
