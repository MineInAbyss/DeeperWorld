package com.mineinabyss.deeperworld.movement

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.entity.TeleportFlag
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.asDeferred
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

class TransitionTeleportHandler(override val entity: Entity, val from: Location, val to: Location) : TeleportHandler {

    override fun handleTeleport() {
        val oldLeashedEntities = leashedEntities()
        val spectators = spectatorEntities()
        val oldVelocity = entity.velocity
        val teleportFlags: Array<TeleportFlag> = listOf(TeleportFlag.Relative.YAW, TeleportFlag.Relative.PITCH, TeleportFlag.Relative.X, TeleportFlag.Relative.Y, TeleportFlag.Relative.Z, TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE).toTypedArray()

        // Unleash all the leashed entities before teleporting them, to prevent leads from dropping.
        // The leashes are restored after teleportation.
        oldLeashedEntities.values.flatten().forEach { it.setLeashHolder(null) }
        spectators.values.flatten().forEach { it.spectatorTarget = null }

        deeperWorld.plugin.launch {
            entity.teleportAsync(to, PlayerTeleportEvent.TeleportCause.PLUGIN, *teleportFlags).asDeferred().await()
            oldLeashedEntities.forEach { (leashHolder, leashEntities) ->
                leashEntities.forEach {
                    it.teleportAsync(entity.location, PlayerTeleportEvent.TeleportCause.PLUGIN, *teleportFlags).asDeferred().await()
                    it.setLeashHolder(leashHolder)
                }
            }
            spectators.forEach { (spectatorTarget, spectators) ->
                spectators.forEach {
                    it.teleportAsync(entity.location, PlayerTeleportEvent.TeleportCause.PLUGIN, *teleportFlags).asDeferred().await()
                    it.spectatorTarget = spectatorTarget
                }
            }

            delay(10.ticks)
            entity.velocity = oldVelocity
            MovementHandler.teleportCooldown -= entity.uniqueId
            MovementHandler.teleportCooldown -= entity.passengers.map { it.uniqueId }.toSet()
        }
    }

    override fun isValidTeleport(): Boolean {
        return true
    }

    private fun spectatorEntities(): Map<Entity, List<Player>> {
        return when (entity) {
            is Player -> mapOf(entity to entity.world.players.filter { it.spectatorTarget?.uniqueId == entity.uniqueId })
            else -> entity.passengers.associateWith { p ->
                p.world.players.filter { it.spectatorTarget?.uniqueId == p.uniqueId }
            }
        }
    }

    private fun leashedEntities(): Map<LivingEntity, List<LivingEntity>> {
        // Max leashed entity range is 10 blocks, therefore these parameter values
        return when (entity) {
            is Player -> mapOf(entity to entity.location
                .getNearbyEntitiesByType(LivingEntity::class.java, 20.0)
                .filter { it.isLeashed && it.leashHolder.uniqueId == entity.uniqueId })

            else -> entity.passengers.filterIsInstance<LivingEntity>().associateWith {
                it.location.getNearbyEntitiesByType(LivingEntity::class.java, 20.0)
                    .filter { it.isLeashed && it.leashHolder.uniqueId == entity.uniqueId }
            }
        }
    }
}
