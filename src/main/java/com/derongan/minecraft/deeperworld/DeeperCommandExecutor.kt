package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.derongan.minecraft.deeperworld.config.DeeperConfig
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.services.canMoveSections
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
        }
        "linfo"{
            playerAction{
                sender.error("Please use /dw linfo or /deeperworld layerinfo instead")
            }
        }
    }
}