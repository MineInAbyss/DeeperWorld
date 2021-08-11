package com.derongan.minecraft.deeperworld.ecs

import com.derongan.minecraft.deeperworld.world.section.SectionKey
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("deeperworld:section")
@AutoscanComponent
//TODO add other features of a section.
data class DeeperWorldSection(val name: SectionKey)