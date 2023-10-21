@file: UseSerializers(WorldSerializer::class, VectorSerializer::class)

package com.mineinabyss.deeperworld.world.section

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.deeperworld.world.Region
import com.mineinabyss.deeperworld.world.getCoordinates
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.VectorSerializer
import com.mineinabyss.idofront.serialization.WorldSerializer
import kotlinx.serialization.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

/**
 * @property region the region within which this section is active
 * @property world the world this section is a part of
 * @property key the key for this section
 * @property aboveKey the previous section that is above this one, or null if none exists TODO by null do you mean SectionKey.TERMINAL?
 * @property belowKey the next section that is below this one, or null if none exists
 * @property referenceTop the reference location between this section and the one above it.
 * This and the section above's [referenceBottom] represent the same location in physical space.
 * @property referenceBottom the reference location between this section and the one below it.
 * This and the section belows' [referenceTop] represent the same location in physical space.
 */
@Serializable
data class Section(
    val name: String? = null,
    val region: Region = Region(0,0,0,1,1,1),
    val world: @Serializable(WorldSerializer::class) World = Bukkit.getWorld("world")!!,
    @SerialName("refTop") private val _refTop: String,
    @YamlComment("refBottom should connect to the refTop of the next section.")
    @SerialName("refBottom") private val _refBottom: String
) {
    @Serializable(LocationSerializer::class)
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val referenceTop = _refTop.toLocation(world)

    @Serializable(LocationSerializer::class)
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val referenceBottom = _refBottom.toLocation(world)

    fun String.toLocation(world: World): Location {
        val (x,y,z) = this.getCoordinates().map { it.toDouble() }
        return Location(world, x, y, z)
    }

    @Transient
    val key: SectionKey = name?.let { AbstractSectionKey.CustomSectionKey(name) }
        ?: AbstractSectionKey.InternalSectionKey()

    @Transient
    internal var aboveKey: SectionKey = SectionKey.TERMINAL

    @Transient
    internal var belowKey: SectionKey = SectionKey.TERMINAL

    override fun toString() = key.toString()
}
