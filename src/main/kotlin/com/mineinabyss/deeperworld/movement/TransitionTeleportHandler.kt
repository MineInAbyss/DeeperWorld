package com.mineinabyss.deeperworld.movement

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.movement.transition.SectionTransition
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.entity.TeleportFlag
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.yield
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.seconds

class TransitionTeleportHandler(val teleportEntity: Entity, val from: Location, val to: Location) : TeleportHandler {

    constructor(teleportEntity: Entity, transition: SectionTransition) : this(teleportEntity, transition.from, transition.to)

    init {
        MovementHandler.teleportCooldown += teleportEntity.uniqueId
    }

    private val teleportFlags: Array<TeleportFlag> = listOf(
        TeleportFlag.Relative.VELOCITY_ROTATION,
        TeleportFlag.Relative.VELOCITY_X,
        TeleportFlag.Relative.VELOCITY_Y,
        TeleportFlag.Relative.VELOCITY_Z
    ).toTypedArray()

    override fun handleTeleport() {
        val (leashedEntities, leashUuids) = leashedEntities().let { it to it.values.flatten().map { it.uniqueId }.toSet() }
        val (spectators, specUuids) = spectatorEntities().let { it to it.values.flatten().map { it.uniqueId }.toSet() }
        val oldVelocity = teleportEntity.velocity

        // Unleash all the leashed entities before teleporting them, to prevent leads from dropping.
        // The leashes are restored after teleportation.
        leashedEntities.values.flatten().forEach { it.setLeashHolder(null) }
        spectators.values.flatten().forEach { it.spectatorTarget = null }
        MovementHandler.teleportCooldown += leashUuids
        MovementHandler.teleportCooldown += specUuids

        deeperWorld.plugin.launch {
            val chunk = to.world.getChunkAtAsync(to).await()
            val addedTicket = chunk.addPluginChunkTicket(deeperWorld.plugin)

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

            MovementHandler.teleportCooldown -= leashUuids
            MovementHandler.teleportCooldown -= specUuids
            MovementHandler.teleportCooldown -= teleportEntity.uniqueId
            if (addedTicket) {
                delay(10.seconds)
                to.chunk.removePluginChunkTicket(deeperWorld.plugin)
            }
        }
    }

    override fun isValidTeleport(): Boolean {
        return true
    }

    private fun spectatorEntities(): Map<Entity, Set<Player>> {
        return when (teleportEntity) {
            is Player -> mapOf(teleportEntity to teleportEntity.world.players.filter { it.spectatorTarget?.uniqueId == teleportEntity.uniqueId }.toSet())
            else -> teleportEntity.passengers.associateWith { p ->
                p.world.players.filter { it.spectatorTarget?.uniqueId == p.uniqueId }.toSet()
            }
        }
    }

    private fun leashedEntities(): Map<LivingEntity, Set<LivingEntity>> {
        // Max leashed entity range is 10 blocks, therefore these parameter values
        return when (teleportEntity) {
            is Player -> mapOf(
                teleportEntity to teleportEntity.location.getNearbyEntitiesByType(LivingEntity::class.java, 20.0) {
                    it.isLeashed && it.leashHolder.uniqueId == teleportEntity.uniqueId
                }.toSet())

            else -> teleportEntity.passengers.filterIsInstance<Player>().associateWith { player ->
                player.location.getNearbyEntitiesByType(LivingEntity::class.java, 20.0) {
                    it.isLeashed && it.leashHolder.uniqueId == player.uniqueId
                }.toSet()
            }
        }
    }
}
