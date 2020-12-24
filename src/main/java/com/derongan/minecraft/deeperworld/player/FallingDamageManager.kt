package com.derongan.minecraft.deeperworld.player

import com.derongan.minecraft.deeperworld.DeeperConfig
import com.derongan.minecraft.deeperworld.getVehicleRecursive
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

internal object FallingDamageManager{
    fun updateFallingDamage(player : Player){
        val actualFallDistance = player.getVehicleRecursive()?.fallDistance ?: player.fallDistance
        
        if (actualFallDistance > DeeperConfig.data.maxFallingDistance
                && !player.isGliding
                && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)) {

            val damageToDeal = (actualFallDistance - DeeperConfig.data.maxFallingDistance) * DeeperConfig.data.fallingDamageMultiplier

            player.damage(0.01) // Damage animation
            player.health = (player.health - damageToDeal).coerceIn(0.0, player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value) // Ignores armor
        }
    }
}