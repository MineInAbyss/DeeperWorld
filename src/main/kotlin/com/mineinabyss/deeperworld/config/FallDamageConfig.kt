@file:UseSerializers(DurationSerializer::class)

package com.mineinabyss.deeperworld.config

import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Duration

@Serializable
class FallDamageConfig(
    val maxSafeDist: Float = -1f,
    val fallDistanceDamageScaler: Double = 0.01,
    val startingDamage: Double = 1.0,
    val hitDelay: Duration = 10.ticks,
    val spawnParticles: Boolean = true
)
