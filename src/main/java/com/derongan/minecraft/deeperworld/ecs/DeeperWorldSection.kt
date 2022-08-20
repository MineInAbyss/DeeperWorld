package com.derongan.minecraft.deeperworld.ecs

import com.mineinabyss.deeperworld.world.section.SectionKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("deeperworld:section")
//TODO add other features of a section.
data class DeeperWorldSection(val name: SectionKey)
