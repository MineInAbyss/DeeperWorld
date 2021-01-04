@file:UseSerializers(WorldSerializer::class)

package com.derongan.minecraft.deeperworld.config

import com.mineinabyss.idofront.serialization.WorldSerializer
import com.mineinabyss.idofront.time.TimeSpan
import com.mineinabyss.idofront.time.TimeSpanSerializer
import com.mineinabyss.idofront.time.seconds
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.World

@Serializable
class TimeConfig(
    val updateInterval: TimeSpan = 1800.seconds,
    val mainWorld: World? = null,
    val syncedWorlds: Map<World, Long> = emptyMap(),
)