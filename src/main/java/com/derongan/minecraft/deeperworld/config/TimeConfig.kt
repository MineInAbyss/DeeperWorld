@file:UseSerializers(WorldSerializer::class)

package com.derongan.minecraft.deeperworld.config

import com.mineinabyss.idofront.serialization.WorldSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.World

@Serializable
class TimeConfig(
        val mainWorld: World? = null,
        val syncedWorlds: Map<World, Int> = emptyMap()
)