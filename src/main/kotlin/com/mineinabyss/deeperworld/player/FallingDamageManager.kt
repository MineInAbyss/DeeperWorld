package com.mineinabyss.deeperworld.player

import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.extensions.getRootVehicle
import org.bukkit.GameMode.ADVENTURE
import org.bukkit.GameMode.SURVIVAL
import org.bukkit.Particle
import org.bukkit.entity.Player

internal object FallingDamageManager {
    fun updateFallingDamage(player: Player) {
        val actualFallDistance = player.getRootVehicle()?.fallDistance ?: player.fallDistance

        if (actualFallDistance > deeperWorld.config.fall.maxSafeDist
            && !player.isGliding
            && !player.allowFlight
            && !player.isDead
            && (player.gameMode == SURVIVAL || player.gameMode == ADVENTURE)
        ) {
            // Always deal a minimum of 1 damage, else the first damage tick could deal (almost) no damage
            val damageToDeal = ((actualFallDistance - deeperWorld.config.fall.maxSafeDist) * deeperWorld.config.fall.fallDistanceDamageScaler)
                .coerceAtLeast(deeperWorld.config.fall.startingDamage)

            player.damage(0.01) // Damage animation
            player.health = (player.health - damageToDeal).coerceAtLeast(0.0)

            if (deeperWorld.config.fall.spawnParticles)
                player.world.spawnParticle(
                    Particle.CLOUD,
                    player.location.apply { y += player.velocity.y * 2 },
                    10, 0.5, 0.75, 0.5, .1
                )
        }
    }
}
