package com.mineinabyss.deeperworld.world

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a region of the world.
 */
@Serializable(with = RegionSerializer::class)
data class Region(val start: CubePoint, val end: CubePoint) {
    val min = CubePoint(min(start.x, end.x), min(start.y, end.y), min(start.z, end.z))
    val max = CubePoint(max(start.x, end.x), max(start.y, end.y), max(start.z, end.z))

    constructor(ax: Int, ay: Int, az: Int, bx: Int, by: Int, bz: Int) : this(
        CubePoint(ax, ay, az),
        CubePoint(bx, by, bz)
    )

    val center: CubePoint get() = start.plus(end).div(2)

    fun contains(x: Int, y: Int, z: Int): Boolean =
        x in min(start.x, end.x)..max(start.x, end.x) &&
                y in min(start.y, end.y)..max(start.y, end.y) &&
                z in min(start.z, end.z)..max(start.z, end.z)

    operator fun contains(p: CubePoint) = contains(p.x, p.y, p.z)
}

@Serializable
data class RegionPoints(val start: String, val end: String)

object RegionSerializer : KSerializer<Region> {
    private val serializer = RegionPoints.serializer()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: Region) {
        val start = value.start.toString()
        val end = value.end.toString()
        encoder.encodeSerializableValue(serializer, RegionPoints(start, end))
    }

    override fun deserialize(decoder: Decoder): Region {
        val decoded = decoder.decodeSerializableValue(serializer)
        val (x, y, z, x2, y2, z2) = decoded.start.getCoordinates() + decoded.end.getCoordinates()
        return Region(x, y, z, x2, y2, z2)
    }
}

private operator fun <E> List<E>.component6(): E = this[5]

internal fun String.getCoordinates() = this.replace(" ", "").split(",", limit = 3).map { it.toIntOrNull() ?: 0 }

