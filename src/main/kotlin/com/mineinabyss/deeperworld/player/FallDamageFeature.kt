package com.mineinabyss.deeperworld.player

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.DeeperWorldConfig
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.sections.SectionFeature
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.idofront.features.task
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Bukkit

/**
 * Damages players that fall for a long enough distance, preventing really large height skips.
 */
val FallDamageFeature = module("fall-damage") {
    singleModule(SectionFeature)
    val config = get<DeeperWorldConfig>().fall
    require(config.enabled) { "Fall damage is disabled in config." }

    val fallDamage by single { new(::FallingDamageManager) }

    val server = Bukkit.getServer()
    val hitDelay = config.hitDelay.coerceAtLeast(1.ticks)


    // Initialize falling damage task
    if (config.maxSafeDist >= 0f && config.fallDistanceDamageScaler >= 0.0) task(deeperWorld.launch {
        while (true) {
            server.onlinePlayers.forEach(fallDamage::updateFallingDamage)
            delay(hitDelay)
        }
    })
}