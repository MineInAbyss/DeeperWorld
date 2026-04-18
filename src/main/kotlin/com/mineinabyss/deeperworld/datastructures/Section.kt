package com.mineinabyss.deeperworld.datastructures

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.idofront.serialization.WorldSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

/**
 * @property region the region within which this section is active
 * @property world the world this section is a part of
 * @property referenceTop the reference location between this section and the one above it.
 * This and the section above's [referenceBottom] represent the same location in physical space.
 * @property referenceBottom the reference location between this section and the one below it.
 * This and the section belows' [referenceTop] represent the same location in physical space.
 */
@Serializable
data class Section(
    val name: String? = null,
    val region: Region,
    val world: @Serializable(WorldSerializer::class) World = Bukkit.getWorlds().first(),
    @SerialName("refTop") private val refTop: CubePoint,
    @YamlComment("refBottom should connect to the refTop of the next section.")
    @SerialName("refBottom") private val refBottom: CubePoint,
) {
    @Transient
    val referenceTop = refTop.toLocation(world)

    @Transient
    val referenceBottom = refBottom.toLocation(world)

    val height: Int get() = region.max.y - region.min.y

    val center: Location
        get() = Location(
            world,
            region.center.x.toDouble(),
            region.center.y.toDouble(),
            region.center.z.toDouble()
        )
}