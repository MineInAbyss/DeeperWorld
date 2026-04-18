package com.mineinabyss.deeperworld.synchronization

import com.fastasyncworldedit.core.util.TaskManager
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.sections.SectionFeature
import com.mineinabyss.deeperworld.sections.correspondingLocation
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.time.ticks
import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operation
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.World
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import kotlinx.coroutines.delay

/**
 * Synchronizes blocks and inventories on section boundaries.
 *
 * The general approach is to copy blocks and access the top section's inventories as a single source of truth.
 */
val SectionSyncFeature = module("section-sync") {
    singleModule(SectionFeature) // Depend on sections
    single<BlockLockerHelpers?> { if (Plugins.isEnabled("BlockLocker")) new(::BlockLockerHelpers) else null }
    listeners(
        new(::SectionSyncListener),
        new(::ExploitPreventionListener),
        new(::ContainerSyncListener)
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
        executes.asPlayer().args("range" to Args.integer(1, 1000)) { range ->
            if (!Plugins.isEnabled("FastAsyncWorldEdit")) fail("Please install FAWE to sync section overlaps")
            val loc = player.location
            val linkedBlock = loc.correspondingLocation?.block ?: fail("Corresponding section not found")

            runCatching {
                val (x, y, z) = player.location
                val pos1 = BlockVector3.at(x + range, y + range, z + range)
                val pos2 = BlockVector3.at(x - range, y - range, z - range)
                val region = CuboidRegion(pos1, pos2)

                val clipboard = BlockArrayClipboard(region)
                val wep = WorldEditPlugin.getInstance().bukkitImplAdapter
                val weWorld: World = wep.adapt(player.world)
                val editSession: EditSession = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(weWorld).limitUnlimited().build()

                val offset = pos2.y().coerceAtLeast(0)
                TaskManager.taskManager().taskNowAsync {
                    player.success("Blocks syncing...")
                    editSession.use { editSession ->
                        // Copy
                        val forwardExtentCopy =
                            ForwardExtentCopy(editSession, region, clipboard, region.minimumPoint)
                        forwardExtentCopy.isCopyingEntities = false
                        forwardExtentCopy.isCopyingBiomes = true
                        Operations.complete(forwardExtentCopy)

                        // Paste
                        val operation: Operation = ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(
                                BlockVector3.at(
                                    linkedBlock.x - range,
                                    linkedBlock.y - range - offset,
                                    linkedBlock.z - range
                                )
                            ).build()
                        Operations.complete(operation)
                    }
                    player.success("Blocks synced (FAWE)")
                }
            }.onFailure {
                player.error("""An error occurred: ${it.message}""")
            }
        }
    }
}
