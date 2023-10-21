package com.mineinabyss.deeperworld

import com.fastasyncworldedit.core.util.TaskManager
import com.mineinabyss.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.deeperworld.services.canMoveSections
import com.mineinabyss.deeperworld.synchronization.sync
import com.mineinabyss.deeperworld.world.section.correspondingLocation
import com.mineinabyss.deeperworld.world.section.correspondingSection
import com.mineinabyss.deeperworld.world.section.getCorrespondingLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.idofront.commands.arguments.booleanArg
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
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
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class DeeperCommandExecutor : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(deeperWorld.plugin) {
        ("deeperworld" / "dw") {
            "reload" {
                action {
                    deeperWorld.plugin.createDeeperWorldContext()
                    sender.success("Reloaded DeeperWorld")
                }
            }
            "tp"(desc = "Enables or disables automatic teleports between sections for a player") {
                val state by booleanArg()
                playerAction {
                    player.canMoveSections = state
                    sender.success("Automatic TP ${if (state) "enabled" else "disabled"} for ${player.name}")
                }
            }
            "tpcorr" {
                playerAction {
                    player.teleport(player.location.correspondingLocation ?: return@playerAction)
                }
            }
            ("layerinfo" / "linfo" / "info")(desc = "Gets information about a players section") {
                playerAction {
                    val section = WorldManager.getSectionFor(player.location)
                    if (section == null)
                        sender.info("${player.name} is not in a managed section")
                    else
                        sender.info("${player.name} is in section ${section.key}")
                }
            }
            "time" {
                val time by intArg()
                "set"(desc = "Set the time of the main synchronization world and the other worlds with their respective offsets") {
                    playerAction {
                        deeperWorld.config.time.mainWorld?.let { world ->
                            world.time = time.toLong()
                        } ?: command.stopCommand("No main world specified for time synchronization. Check the config!")

                        deeperWorld.config.time.syncedWorlds.forEach { (world, offset) ->
                            world.time = (time.toLong() + offset) % FULL_DAY_TIME
                        }

                        sender.success("Set synced time to $time")
                    }
                }
                "add"(desc = "Add to the main synchronization world time and the other worlds with their respective offsets") {
                    playerAction {
                        deeperWorld.config.time.mainWorld?.let { mainWorld ->
                            mainWorld.time += time.toLong()

                            deeperWorld.config.time.syncedWorlds.forEach { (world, offset) ->
                                world.time = (mainWorld.time + offset) % FULL_DAY_TIME
                            }

                            sender.success("Added $time to synced time")
                        } ?: command.stopCommand("No main world specified for time synchronization. Check the config!")
                    }
                }
            }
            "sync"(desc = "Sync blocks in range") {
                val range by intArg()
                playerAction {
                    val section =
                        player.location.section ?: run {
                            sender.error("${player.name} is not in a managed section")
                            return@playerAction
                        }

                    when {
                        Plugins.isEnabled("FastAsyncWorldEdit") -> {
                            try {
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
                                        .world(weWorld)
                                        .limitUnlimited()
                                        .build()

                                val loc = player.location
                                val linkedSection =
                                    loc.correspondingSection ?: error("Corresponding Section not found")

                                val linkedBlock = loc.getCorrespondingLocation(section, linkedSection)?.block
                                    ?: error("Corresponding Location not found")

                                val offset = if (pos2.y < 0) pos2.y else 0
                                TaskManager.taskManager().taskNowAsync {
                                    player.success("Blocks syncing...")
                                    editSession.use { editSession ->
                                        // Copy
                                        val forwardExtentCopy =
                                            ForwardExtentCopy(
                                                editSession, region, clipboard, region.minimumPoint
                                            )
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
                                            )
                                            .build()
                                        Operations.complete(operation)
                                    }
                                    player.success("Blocks synced (FAWE)")
                                }
                            } catch (e: Exception) {
                                player.error("""An error occurred: ${e.message}""")
                            }
                        }
                        range <= 100 -> {
                            // Get blocks in range specified
                            for (x in -range..range) {
                                for (y in -range..range) {
                                    for (z in -range..range) {
                                        val block = player.world.getBlockAt(
                                            (player.location.x + x).toInt(),
                                            (player.location.y + y).toInt(),
                                            (player.location.z + z).toInt()
                                        )

                                        block.sync { original, corr ->
                                            if (original.type != corr.type) {
                                                corr.blockData = original.blockData.clone()
                                            }
                                        }
                                    }
                                }
                            }

                            player.success("Blocks synced")
                        }
                        else -> {
                            sender.error("Please use a range smaller than 100 blocks, or install FAWE to use a larger range")
                        }
                    }
                }
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf(
                "reload",
                "linfo",
                "tp",
                "time",
                "sync"
            ).filter { it.startsWith(args[0]) }
            2 -> {
                when (args[0]) {
                    "tp" -> listOf("on", "off")
                    "time" -> listOf("set", "add")
                    else -> listOf()
                }
            }
            else -> listOf()
        }
    }
}
