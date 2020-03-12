package com.derongan.minecraft.deeperworld.world.section

import com.derongan.minecraft.deeperworld.world.Region
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
 * This and the section belows's [referenceTop] represent the same location in physical space.
 */
data class Section(val region: Region,
                   val world: World,
                   val key: SectionKey,
                   @get:JvmName("getKeyForSectionAbove") val aboveKey: SectionKey = SectionKey.TERMINAL,
                   @get:JvmName("getKeyForSectionBelow") val belowKey: SectionKey = SectionKey.TERMINAL,
                   @get:JvmName("getReferenceLocationTop") val referenceTop: Location? = null,
                   @get:JvmName("getReferenceLocationBottom") val referenceBottom: Location? = null) {

    override fun toString(): String {
        return key.toString()
    }
}