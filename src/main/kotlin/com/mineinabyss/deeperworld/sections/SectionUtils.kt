@file:JvmMultifileClass
@file:JvmName("SectionUtils")

package com.mineinabyss.deeperworld.sections

import com.mineinabyss.deeperworld.datastructures.KeyedSection
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.movement.transition.LocationSection
import org.bukkit.Location

val Location.section: LocationSection? get() = deeperWorld.sections[this]

/**
 * The corresponding section which overlaps with this location's section. Will be null if the section is not in an
 * overlap, even if there is a section above or below, since it's unclear which section becomes the corresponding one.
 */
val Location.correspondingSection: KeyedSection? get() = section?.section

/**
 * The location as it would be in the [correspondingSection]. Will be null if the section is not in an overlap.
 */
val Location.correspondingLocation: Location? get() = section?.linkedLocation

val Location.inSectionOverlap: Boolean get() = section?.inOverlap == true

val Location.inSection: Boolean get() = section?.section != null

val Location.inSectionTransition: Boolean get() = section?.inTransition == true
