package com.mineinabyss.deeperworld

import com.fastasyncworldedit.core.util.TaskManager
import com.mineinabyss.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.deeperworld.services.canMoveSections
import com.mineinabyss.deeperworld.synchronization.sync
import com.mineinabyss.deeperworld.world.section.*
import com.mineinabyss.idofront.commands.brigadier.*
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.Plugins
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
import org.bukkit.entity.Player

object DeeperCommands {
    fun registerCommands() {
        deeperWorld.plugin.commands {
            ("deeperworld" / "dw") {
                ("reload" / "rl") {
                    executes {
                        deeperWorld.plugin.createDeeperWorldContext()
                        sender.success("Reloaded DeeperWorld")
                    }
                }
                "tp" {
                    executes(ArgumentTypes.players().resolve()) { players ->
                        players.forEach { player ->
                            val canMoveSections = !player.canMoveSections
                            player.canMoveSections = canMoveSections
                        }
                        val msg = players.joinToString { (if (!it.canMoveSections) "<red>" else "").plus(it.name) }
                        sender.success("Automatic TP handled for $msg")
                    }
                }
                ("layerinfo" / "linfo" / "info" / "layers" / "layers") {
                    playerExecutes {
                        val section = WorldManager.getSectionFor(player.location)
                        if (section == null) sender.info("${player.name} is not in a managed section")
                        else sender.info("${player.name} is in section ${section.key}")
                    }
                }
                "time" {
                    "set" {
                        playerExecutes(ArgumentTypes.time()) { time ->
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
                        playerExecutes(ArgumentTypes.time()) { time ->
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
                    playerExecutes(Args.integer(1, 1000)) { range ->
                        val section = player.location.section ?: run {
                            sender.error("${player.name} is not in a managed section")
                            return@playerExecutes
                        }

                        when {
                            Plugins.isEnabled("FastAsyncWorldEdit") -> runCatching {
                                val pos1 = BlockVector3.at(
                                    (player.location.x + range),
                                    (player.location.y + range),
                                    (player.location.z + range)
                                )
                                val pos2 = BlockVector3.at(
                                    (player.location.x - range),
                                    (player.location.y - range),
                                    (player.location.z - range)
                                )

                                val region = CuboidRegion(pos1, pos2)
                                val clipboard = BlockArrayClipboard(region)
                                val wep = WorldEditPlugin.getInstance().bukkitImplAdapter
                                val weWorld: World = wep.adapt(player.world)
                                val editSession: EditSession = WorldEdit.getInstance().newEditSessionBuilder()
                                    .world(weWorld).limitUnlimited().build()

                                val loc = player.location
                                val linkedSection = loc.correspondingSection ?: error("Corresponding Section not found")

                                val linkedBlock = loc.getCorrespondingLocation(section, linkedSection)?.block
                                    ?: error("Corresponding Location not found")

                                val offset = pos2.y().coerceAtLeast(0)
                                TaskManager.taskManager().taskNowAsync {
                                    player.success("Blocks syncing...")
                                    editSession.use { editSession ->
                                        // Copy
                                        val forwardExtentCopy = ForwardExtentCopy(editSession, region, clipboard, region.minimumPoint)
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

                            range <= 100 -> {
                                // Get blocks in range specified
                                for (x in -range..range) for (y in -range..range) for (z in -range..range) {
                                    val block = player.world.getBlockAt(
                                        (player.location.x + x).toInt(),
                                        (player.location.y + y).toInt(),
                                        (player.location.z + z).toInt()
                                    )

                                    block.sync { original, corr ->
                                        if (original.type != corr.type) corr.blockData = original.blockData.clone()
                                    }
                                }

                                player.success("Blocks synced")
                            }

                            else -> sender.error("Please use a range smaller than 100 blocks, or install FAWE to use a larger range")
                        }
                    }
                }
                "depth" {
                    val playerArg = ArgumentTypes.player()
                        .resolve()
                        .default { listOf(executor as? Player ?: fail("Receiver must be a player or pass a player as argument")) }

                    executes(playerArg) { players ->
                        val player = players.single()

                        WorldManager.getDepthFor(player.location)?.let{
                            if (sender is Player){
                                sender.success("Your depth is $it blocks")
                            }
                            else{
                                sender.success("Depth of player ${player.name} is $it blocks")
                            }

                        } ?: sender.error("${player.name} is not in a managed section")
                    }
                }
            }
        }
    }
}
