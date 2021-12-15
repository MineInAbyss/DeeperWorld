@file:UseSerializers(WorldSerializer::class, DurationSerializer::class)

package com.derongan.minecraft.deeperworld.config

import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.WorldSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.World
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
class TimeConfig(
    val updateInterval: Duration = 1800.seconds,
    val mainWorld: World? = null,
    val syncedWorlds: Map<World, Long> = emptyMap(),
)
