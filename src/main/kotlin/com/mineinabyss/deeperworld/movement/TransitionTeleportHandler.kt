package com.mineinabyss.deeperworld.movement

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.entity.TeleportFlag
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.asDeferred
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

class TransitionTeleportHandler(val player: Player, val from: Location, val to: Location) : TeleportHandler {

    override fun handleTeleport() {
        val oldLeashedEntities = player.getLeashedEntities()
        val spectators = player.world.players.filter { it.spectatorTarget?.uniqueId == player.uniqueId }
        val teleportFlags: Array<TeleportFlag> = listOf(TeleportFlag.Relative.YAW, TeleportFlag.Relative.PITCH, TeleportFlag.Relative.X, TeleportFlag.Relative.Y, TeleportFlag.Relative.Z, TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE).toTypedArray()

        // Unleash all the leashed entities before teleporting them, to prevent leads from dropping.
        // The leashes are restored after teleportation.
        for (it in oldLeashedEntities) it.setLeashHolder(null)
        for (it in spectators) it.spectatorTarget = null

        deeperWorld.plugin.launch {
            player.teleportAsync(to, PlayerTeleportEvent.TeleportCause.PLUGIN, *teleportFlags).asDeferred().await()
            oldLeashedEntities.forEach { leashEntity ->
                leashEntity.teleportAsync(player.location, PlayerTeleportEvent.TeleportCause.PLUGIN, *teleportFlags).asDeferred().await()
                leashEntity.setLeashHolder(player)
            }
            spectators.forEach { spectator ->
                spectator.teleportAsync(player.location, PlayerTeleportEvent.TeleportCause.PLUGIN, *teleportFlags).asDeferred().await()
                spectator.spectatorTarget = player
            }

            delay(10.ticks)
            MovementHandler.teleportCooldown -= player.uniqueId
        }
    }

    override fun isValidTeleport(): Boolean {
        return true
    }

    private fun Player.getLeashedEntities(): List<LivingEntity> {
        // Max leashed entity range is 10 blocks, therefore these parameter values
        return location.getNearbyEntitiesByType(LivingEntity::class.java, 20.0)
            .filter { it.isLeashed && it.leashHolder.uniqueId == this.uniqueId }
    }
}
