package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.derongan.minecraft.deeperworld.config.DeeperConfig
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.services.canMoveSections
import com.derongan.minecraft.deeperworld.synchronization.sync
import com.derongan.minecraft.deeperworld.world.section.correspondingSection
import com.derongan.minecraft.deeperworld.world.section.getCorrespondingLocation
import com.derongan.minecraft.deeperworld.world.section.section
import com.fastasyncworldedit.core.util.EditSessionBuilder
import com.fastasyncworldedit.core.util.TaskManager
import com.mineinabyss.idofront.commands.CommandHolder
import com.mineinabyss.idofront.commands.arguments.booleanArg
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operation
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.World


@ExperimentalCommandDSL
object DeeperCommandExecutor : IdofrontCommandExecutor() {
    override val commands: CommandHolder = commands(deeperWorld) {
        ("deeperworld" / "dw") {
            "tp"(desc = "Enables or disables automatic teleports between sections for a player") {
                val state by booleanArg()
                playerAction {
                    player.canMoveSections = state
                    sender.success("Automatic TP ${if(state) "enabled" else "disabled"} for ${player.name}")
                }
            }
            ("layerinfo" / "linfo")(desc = "Gets information about a players section") {
                playerAction {
                    val section = WorldManager.getSectionFor(player.location)
                    if (section == null)
                        sender.info("${player.name} is not in a managed section")
                    else
                        sender.info("${player.name} is in section ${section.key}")
                }
            }
            "info" {
                playerAction {
                    sender.error("Please use /dw linfo or /deeperworld layerinfo instead")
                }
            }
            "time" {
                val time by intArg()
                "set"(desc = "Set the time of the main synchronization world and the other worlds with their respective offsets") {
                    playerAction {
                        DeeperConfig.data.time.mainWorld?.let { world ->
                            world.time = time.toLong()
                        } ?: command.stopCommand("No main world specified for time synchronization. Check the config!")

                        DeeperConfig.data.time.syncedWorlds.forEach { (world, offset) ->
                            world.time = (time.toLong() + offset) % FULL_DAY_TIME
                        }

                        sender.success("Set synced time to $time")
                    }
                }
                "add"(desc = "Add to the main synchronization world time and the other worlds with their respective offsets") {
                    playerAction {
                        DeeperConfig.data.time.mainWorld?.let { mainWorld ->
                            mainWorld.time += time.toLong()

                            DeeperConfig.data.time.syncedWorlds.forEach { (world, offset) ->
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
                        DeeperContext.isFAWELoaded -> {
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
                                val wep = WorldEditPlugin.getInstance().bukkitImplAdapter;
                                val weWorld: World = wep.adapt(player.world)
                                val editSession: EditSession = EditSessionBuilder(weWorld)
                                    .limitUnlimited()
                                    .build()

                                val loc = player.location
                                val linkedSection =
                                    loc.correspondingSection ?: error("Corresponding Section not found")

                                val linkedBlock = loc.getCorrespondingLocation(section, linkedSection)?.block
                                    ?: error("Corresponding Location not found")

                                val offset = if (pos2.y < 0) pos2.y else 0
                                TaskManager.IMP.taskNowAsync {
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
        "linfo"{
            playerAction{
                sender.error("Please use /dw linfo or /deeperworld layerinfo instead")
            }
        }
    }
}