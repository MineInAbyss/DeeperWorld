package com.derongan.minecraft.deeperworld.config

import kotlinx.serialization.Serializable

@Serializable
class FallDamageConfig(
    val maxSafeDist: Float = -1f,
    val fallDistanceDamageScaler: Double = 0.01,
    val startingDamage: Double = 1.0,
    val hitDelay: Long = 10,
)
