package com.derongan.minecraft.deeperworld.world

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a region of the world. Contains all Y values.
 */
@Serializable(with = RegionSerializer::class)
class Region(val a: Point, val b: Point) {
    constructor(ax: Int, az: Int, bx: Int, bz: Int) : this(Point(ax, az), Point(bx, bz))

    val center: Point get() = a.plus(b).div(2)

    fun contains(x: Int, z: Int): Boolean = x in min(a.x, b.x)..max(a.x, b.x) && z in min(a.z, b.z)..max(a.z, b.z)

    operator fun contains(p: Point) = contains(p.x, p.z)
}

object RegionSerializer : KSerializer<Region> {
    private val serializer = ListSerializer(Int.serializer())
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: Region) {
        val (x, y) = value.a
        val (x2, y2) = value.b
        encoder.encodeSerializableValue(serializer, listOf(x, y, x2, y2))
    }

    override fun deserialize(decoder: Decoder): Region {
        val (x, y, x2, y2) = decoder.decodeSerializableValue(serializer)
        return Region(x, y, x2, y2)
    }
}