package com.derongan.minecraft.deeperworld.config

import com.mineinabyss.idofront.time.TimeSpan
import com.mineinabyss.idofront.time.TimeSpanSerializer
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.Serializable

@Serializable
class FallDamageConfig(
    val maxSafeDist: Float = -1f,
    val fallDistanceDamageScaler: Double = 0.01,
    val startingDamage: Double = 1.0,
    val hitDelay: TimeSpan = 10.ticks,
    val spawnParticles: Boolean = true
)
