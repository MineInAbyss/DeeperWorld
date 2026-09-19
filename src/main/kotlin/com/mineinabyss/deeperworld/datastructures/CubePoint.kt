package com.mineinabyss.deeperworld.datastructures

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Location
import org.bukkit.World

/**
 * Represents a single X/Y/Z cube in a minecraft world.
 *
 * Serialized as `x,y,z`
 */
@Serializable(with = CubePoint.Serializer::class)
data class CubePoint(val x: Int, val y: Int, val z: Int) {
    operator fun plus(other: CubePoint) = CubePoint(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: CubePoint) = CubePoint(x - other.x, y - other.y, z - other.z)

    operator fun div(o: Float) = CubePoint((x / o).toInt(), (y / o).toInt(), (z / o).toInt())

    operator fun div(o: Int) = CubePoint(x / o, y / o, z / o)

    override fun toString() = "$x,$y,$z"

    fun toLocation(world: World) = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

    object Serializer : KSerializer<CubePoint> {
        override val descriptor = PrimitiveSerialDescriptor("point", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): CubePoint {
            val encoded = decoder.decodeString()
            val parts = encoded.replace(" ", "").split(",", limit = 3)
            require(parts.size == 3) { "Expected 3 comma-separated coordinates but got \"$encoded\"" }
            val (x, y, z) = parts.map { requireNotNull(it.toIntOrNull()) { "Invalid coordinate \"$it\" in \"$encoded\"" } }
            return CubePoint(x, y, z)
        }

        override fun serialize(encoder: Encoder, value: CubePoint) {
            encoder.encodeString(value.toString())
        }
    }
}
