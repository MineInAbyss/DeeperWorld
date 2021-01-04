package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.config.DeeperConfig
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.services.canMoveSections
import com.mineinabyss.idofront.commands.CommandHolder
import com.mineinabyss.idofront.commands.arguments.booleanArg
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success

@ExperimentalCommandDSL
object DeeperCommandExecutor : IdofrontCommandExecutor() {
    override val commands: CommandHolder = commands(deeperWorld) {
        ("deeperworld" / "dw") {
            "tp" {
                val state by booleanArg()
                playerAction{
                    if(state){
                        player.canMoveSections = true
                        sender.success("Automatic TP enabled for ${player.name}")
                    }
                    else{
                        player.canMoveSections = false
                        sender.success("Automatic TP disabled for ${player.name}")
                    }
                }
            }
            "linfo" {
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
                "set"{
                    action {
                        DeeperConfig.data.time.mainWorld?.let { world ->
                            world.time = time.toLong()
                        } ?: return@action

                        DeeperConfig.data.time.syncedWorlds.forEach { (world, offset) ->
                            world.time = time.toLong() + offset
                        }
                    }
                }
                "add"{
                    action {
                        DeeperConfig.data.time.mainWorld?.let { mainWorld ->
                            mainWorld.time += time.toLong()

                            DeeperConfig.data.time.syncedWorlds.forEach { (world, offset) ->
                                world.time = (mainWorld.time + offset) + time.toLong()
                            }
                        }
                    }
                }
            }
        }
    }
}