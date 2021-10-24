package com.derongan.minecraft.deeperworld.world

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a region of the world.
 */
@Serializable(with = RegionSerializer::class)
class Region(val a: CubePoint, val b: CubePoint) {
    constructor(ax: Int, ay: Int, az: Int, bx: Int, by: Int, bz: Int) : this(
        CubePoint(ax, ay, az),
        CubePoint(bx, by, bz)
    )

    val center: CubePoint get() = a.plus(b).div(2)

    fun contains(x: Int, y: Int, z: Int): Boolean = x in min(a.x, b.x)..max(a.x, b.x) &&
            y in min(a.y, b.y)..max(a.y, b.y) &&
            z in min(a.z, b.z)..max(a.z, b.z)

    operator fun contains(p: CubePoint) = contains(p.x, p.y, p.z)
}

object RegionSerializer : KSerializer<Region> {
    private val serializer = ListSerializer(Int.serializer())
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: Region) {
        val (x, y, z) = value.a
        val (x2, y2, z2) = value.b
        encoder.encodeSerializableValue(serializer, listOf(x, y, z, x2, y2, z2))
    }

    override fun deserialize(decoder: Decoder): Region {
        val (x, y, z, x2, y2, z2) = decoder.decodeSerializableValue(serializer)
        return Region(x, y, z, x2, y2, z2)
    }
}

private operator fun <E> List<E>.component6(): E = this[5]


