@file:JvmMultifileClass
@file:JvmName("SectionUtils")

package com.mineinabyss.deeperworld.sections

import com.mineinabyss.deeperworld.datastructures.Section
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.movement.transition.SectionLocation
import org.bukkit.Location

val Location.withSection: SectionLocation? get() = deeperWorld.sections[this]

val Location.section: Section? get() = withSection?.section

/**
 * The corresponding section which overlaps with this location's section. Will be null if the section is not in an
 * overlap, even if there is a section above or below, since it's unclear which section becomes the corresponding one.
 */
val Location.correspondingSection: Section? get() = withSection?.section

/**
 * The location as it would be in the [correspondingSection]. Will be null if the section is not in an overlap.
 */
val Location.correspondingLocation: Location? get() = withSection?.linkedLocation

val Location.inSectionOverlap: Boolean get() = withSection?.inOverlap == true

val Location.inSection: Boolean get() = withSection?.section != null

val Location.inSectionTransition: Boolean get() = withSection?.inTransition == true
