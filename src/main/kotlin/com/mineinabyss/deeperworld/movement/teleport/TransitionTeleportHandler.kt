package com.mineinabyss.deeperworld.movement.teleport

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.movement.MovementHandler
import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.entity.TeleportFlag
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class TransitionTeleportHandler: TeleportHandler {
    override val isValid: Boolean = true

    private val teleportCooldown = mutableSetOf<UUID>()
    private val teleportFlags: Array<TeleportFlag> = arrayOf(
        TeleportFlag.Relative.VELOCITY_ROTATION,
        TeleportFlag.Relative.VELOCITY_X,
        TeleportFlag.Relative.VELOCITY_Y,
        TeleportFlag.Relative.VELOCITY_Z
    )

    override fun handleTeleport(entity: Entity, transition: SectionTransition) {
        if(entity.uniqueId in teleportCooldown) return
        //TODO used to pass entity.vehicle ?: entity, check this is valid
        val teleportEntity = entity.vehicle ?: entity

        val (leashedEntities, leashUuids) = leashedEntities(teleportEntity).let { it to it.values.flatten().map { it.uniqueId }.toSet() }
        val (spectators, specUuids) = spectatorEntities(teleportEntity).let { it to it.values.flatten().map { it.uniqueId }.toSet() }
        val oldVelocity = teleportEntity.velocity

        // Unleash all the leashed entities before teleporting them, to prevent leads from dropping.
        // The leashes are restored after teleportation.
        leashedEntities.values.flatten().forEach { it.setLeashHolder(null) }
        spectators.values.flatten().forEach { it.spectatorTarget = null }
        teleportCooldown += leashUuids
        teleportCooldown += specUuids

        val to = transition.to
        deeperWorld.launch {
            val chunk = to.world.getChunkAtAsync(to).await()
            val addedTicket = chunk.addPluginChunkTicket(deeperWorld)

            if (teleportEntity.teleportAsync(to, PlayerTeleportEvent.TeleportCause.PLUGIN, *teleportFlags).await()) {
                teleportEntity.velocity = oldVelocity
                leashedEntities.forEach { (leashHolder, leashEntities) ->
                    leashEntities.forEach {
                        it.teleportAsync(leashHolder.location, PlayerTeleportEvent.TeleportCause.PLUGIN, *teleportFlags).await()
                        delay(2.ticks)
                        it.setLeashHolder(leashHolder)
                    }
                }

                spectators.forEach { (spectatorTarget, spectators) ->
                    spectators.forEach {
                        it.teleportAsync(spectatorTarget.location).await()
                        delay(2.ticks)
                        it.spectatorTarget = spectatorTarget
                        delay(2.ticks)
                        it.spectatorTarget = null
                        delay(2.ticks)
                        it.spectatorTarget = spectatorTarget
                    }
                }
            }

            teleportCooldown -= leashUuids
            teleportCooldown -= specUuids
            teleportCooldown -= teleportEntity.uniqueId
            if (addedTicket) {
                delay(10.seconds)
                to.chunk.removePluginChunkTicket(deeperWorld)
            }
        }
    }

    private fun spectatorEntities(teleportEntity: Entity): Map<Entity, Set<Player>> {
        return when (teleportEntity) {
            is Player -> mapOf(teleportEntity to teleportEntity.world.players.filter { it.spectatorTarget?.uniqueId == teleportEntity.uniqueId }.toSet())
            else -> teleportEntity.passengers.associateWith { p ->
                p.world.players.filter { it.spectatorTarget?.uniqueId == p.uniqueId }.toSet()
            }
        }
    }

    private fun leashedEntities(teleportEntity: Entity): Map<LivingEntity, Set<LivingEntity>> {
        // Max leashed entity range is 10 blocks, therefore these parameter values
        return when (teleportEntity) {
            is Player -> mapOf(
                teleportEntity to teleportEntity.location.getNearbyEntitiesByType(LivingEntity::class.java, 20.0) {
                    it.isLeashed && it.leashHolder.uniqueId == teleportEntity.uniqueId
                }.toSet()
            )

            else -> teleportEntity.passengers.filterIsInstance<Player>().associateWith { player ->
                player.location.getNearbyEntitiesByType(LivingEntity::class.java, 20.0) {
                    it.isLeashed && it.leashHolder.uniqueId == player.uniqueId
                }.toSet()
            }
        }
    }
}