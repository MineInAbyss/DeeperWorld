package com.derongan.minecraft.deeperworld.player

import com.derongan.minecraft.deeperworld.config.DeeperConfig
import com.derongan.minecraft.deeperworld.extensions.getRootVehicle
import org.bukkit.GameMode.ADVENTURE
import org.bukkit.GameMode.SURVIVAL
import org.bukkit.Particle
import org.bukkit.entity.Player

internal object FallingDamageManager {
    fun updateFallingDamage(player: Player) {
        val actualFallDistance = player.getRootVehicle()?.fallDistance ?: player.fallDistance
        val fallConfig = DeeperConfig.data.fall

        if (actualFallDistance > fallConfig.maxSafeDist
            && !player.isGliding
            && !player.allowFlight
            && !player.isDead
            && (player.gameMode == SURVIVAL || player.gameMode == ADVENTURE)
        ) {
            // Always deal a minimum of 1 damage, else the first damage tick could deal (almost) no damage
            val damageToDeal = ((actualFallDistance - fallConfig.maxSafeDist) * fallConfig.fallDistanceDamageScaler)
                .coerceAtLeast(fallConfig.startingDamage)

            player.damage(0.01) // Damage animation
            player.health = (player.health - damageToDeal).coerceAtLeast(0.0)

            if (fallConfig.spawnParticles)
                player.world.spawnParticle(
                    Particle.CLOUD,
                    player.location.apply { y += player.velocity.y * 2 },
                    10, 0.5, 0.75, 0.5, .1
                )
        }
    }
}
