package com.mineinabyss.deeperworld.synchronization

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.DeeperWorldConfig.SyncMode
import com.mineinabyss.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.sections.SectionFeature
import com.mineinabyss.dependencies.*
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.default
import com.mineinabyss.idofront.commands.brigadier.map
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Synchronizes blocks and inventories on section boundaries.
 *
 * The general approach is to copy blocks and access the top section's inventories as a single source of truth.
 */
val SectionSyncFeature = module("section-sync") {
    import(singleModule(SectionFeature)) // Depend on sections
    if (Plugins.isEnabled("BlockLocker")) single<BlockLockerHelpers> { new(::BlockLockerHelpers) }
    listeners(
        SectionSyncListener(getOrNull(), get()),
        ContainerSyncListener(getOrNull(), get()),
        new(::ExploitPreventionListener),
    )

    // Initialize time synchronization task
    if (deeperWorld.config.time.syncedWorlds.isNotEmpty()) {
        deeperWorld.config.time.mainWorld?.let { mainWorld ->
            val updateInterval = deeperWorld.config.time.updateInterval.coerceAtLeast(1.ticks)
            deeperWorld.launch {
                while (true) {
                    val mainWorldTime = mainWorld.time
                    deeperWorld.config.time.syncedWorlds.forEach { (world, offset) ->
                        world.time = (mainWorldTime + offset) % FULL_DAY_TIME
                    }
                    delay(updateInterval)
                }
            }
        }
    }
}.mainCommand {
    "time" {
        "set" {
            executes.asPlayer().args("time" to ArgumentTypes.time()) { time ->
                deeperWorld.config.time.mainWorld?.let { world ->
                    world.time = time.toLong()
                } ?: fail("No main world specified for time synchronization. Check the config!")

                deeperWorld.config.time.syncedWorlds.forEach { (world, offset) ->
                    world.time = (time.toLong() + offset) % FULL_DAY_TIME
                }

                sender.success("Set synced time to $time")
            }
        }
        "add" {
            executes.asPlayer().args("time" to ArgumentTypes.time()) { time ->
                deeperWorld.config.time.mainWorld?.let { mainWorld ->
                    mainWorld.time += time.toLong()

                    deeperWorld.config.time.syncedWorlds.forEach { (world, offset) ->
                        world.time = (mainWorld.time + offset) % FULL_DAY_TIME
                    }

                    sender.success("Added $time to synced time")
                } ?: fail("No main world specified for time synchronization. Check the config!")
            }
        }
    }
    "sync" {
        executes.asPlayer().args(
            "range" to Args.integer(1, 1000),
            "mode" to Args.word()
                .oneOf(SyncMode.entries.map { it.name.lowercase() })
                .map { SyncMode.valueOf(it.uppercase()) }
                .default { deeperWorld.config.sync.defaultMode },
        ) { range, mode ->
            val region = SyncRegion.around(player.location, range) ?: fail("Corresponding section not found")
            if (mode == SyncMode.FAWE && !Plugins.isEnabled("FastAsyncWorldEdit"))
                fail("FAWE mode requires FastAsyncWorldEdit, use native mode instead")
            if (mode == SyncMode.NATIVE && region.overlapsItself)
                fail("Source and target overlap, which native mode cannot copy without overwriting its own input. Use FAWE mode or a smaller range")

            if (mode == SyncMode.NATIVE && region.volume > deeperWorld.config.sync.confirmAboveBlocks) {
                pendingSyncs.values.removeIf { it.expiresAt <= System.currentTimeMillis() }
                pendingSyncs[player.uniqueId] = PendingSync(region, System.currentTimeMillis() + CONFIRM_TIMEOUT_MS)
                player.warn(
                    "This will copy ${"%,d".format(region.volume)} blocks on the main thread without FAWE and may take a long time. " +
                        "Run /dw sync confirm within ${CONFIRM_TIMEOUT_MS / 1000}s to proceed"
                )
                return@args
            }
            startSync(player, region, mode)
        }
        "confirm" {
            executes.withPermission(parentPermission!!).asPlayer {
                val pending = pendingSyncs.remove(player.uniqueId)
                    ?.takeIf { it.expiresAt > System.currentTimeMillis() }
                    ?: fail("No sync is waiting for confirmation")
                startSync(player, pending.region, SyncMode.NATIVE)
            }
        }
        "cancel" {
            executes.withPermission(parentPermission!!).asPlayer {
                runningSyncs.remove(player.uniqueId)?.cancel() ?: fail("You have no native sync running")
                player.success("Sync cancelled, blocks copied so far are kept")
            }
        }
    }
}

private const val CONFIRM_TIMEOUT_MS = 30_000L

private class PendingSync(val region: SyncRegion, val expiresAt: Long)

private val pendingSyncs = mutableMapOf<UUID, PendingSync>()
private val runningSyncs = mutableMapOf<UUID, Job>()

private fun startSync(player: Player, region: SyncRegion, mode: SyncMode) {
    player.success("Syncing ${"%,d".format(region.volume)} blocks (${mode.name.lowercase()})...")
    when (mode) {
        SyncMode.FAWE -> FaweSync.copy(region) { error ->
            if (error == null) player.success("Blocks synced (FAWE)")
            else player.error("Sync failed: ${error.message}")
        }

        SyncMode.NATIVE -> {
            if (runningSyncs[player.uniqueId]?.isActive == true) {
                player.error("You already have a sync running, use /dw sync cancel to stop it")
                return
            }
            var lastReport = System.currentTimeMillis()
            val job = deeperWorld.launch {
                NativeSync.copy(region) { copied ->
                    if (System.currentTimeMillis() - lastReport < PROGRESS_INTERVAL_MS) return@copy
                    lastReport = System.currentTimeMillis()
                    player.info("Synced ${copied * 100 / region.volume}% (${"%,d".format(copied)} blocks)")
                }
                player.success("Blocks synced (native)")
            }
            runningSyncs[player.uniqueId] = job
            job.invokeOnCompletion { error ->
                runningSyncs.remove(player.uniqueId, job)
                if (error != null && error !is CancellationException) player.error("Sync failed: ${error.message}")
            }
        }
    }
}

private const val PROGRESS_INTERVAL_MS = 5_000L
