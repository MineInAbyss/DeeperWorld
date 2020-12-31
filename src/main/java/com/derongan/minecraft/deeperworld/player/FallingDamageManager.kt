package com.derongan.minecraft.deeperworld.player

import com.derongan.minecraft.deeperworld.DeeperConfig
import com.derongan.minecraft.deeperworld.getVehicleRecursive
import org.bukkit.GameMode
import org.bukkit.entity.Player

internal object FallingDamageManager{
    fun updateFallingDamage(player : Player){
        val actualFallDistance = player.getVehicleRecursive()?.fallDistance ?: player.fallDistance
        
        if (actualFallDistance > DeeperConfig.data.maxSafeFallingDistance
                && !player.isGliding
                && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)) {
            // Always deal a minimum of 1 damage, else the first damage tick could deal (almost) no damage
            val damageToDeal = ((actualFallDistance - DeeperConfig.data.maxSafeFallingDistance) * DeeperConfig.data.fallingDamageMultiplier).coerceAtLeast(1.0)
            player.damage(0.01) // Damage animation
            player.health = (player.health - damageToDeal).coerceAtLeast(0.0)
        }
    }
}