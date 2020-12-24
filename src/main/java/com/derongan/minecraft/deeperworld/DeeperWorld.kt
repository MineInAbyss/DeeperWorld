package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.listeners.MovementListener
import com.derongan.minecraft.deeperworld.listeners.PlayerListener
import com.derongan.minecraft.deeperworld.services.PlayerManager
import com.derongan.minecraft.deeperworld.player.PlayerManagerImpl
import com.derongan.minecraft.deeperworld.synchronization.SectionSyncListener
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.synchronization.ContainerSyncListener
import com.derongan.minecraft.deeperworld.synchronization.ExploitPreventionListener
import com.derongan.minecraft.deeperworld.world.WorldManagerImpl
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class DeeperWorld : JavaPlugin() {
    @ExperimentalCommandDSL
    override fun onEnable() {
        saveDefaultConfig()

        registerService<WorldManager>(WorldManagerImpl(config))

        DeeperConfig

        registerService<PlayerManager>(PlayerManagerImpl())
        registerEvents(
                MovementListener,
                PlayerListener,
                SectionSyncListener,
                ExploitPreventionListener,
                ContainerSyncListener
        )

        //register command executor
        DeeperCommandExecutor

        Bukkit.getScheduler().schedule(this){
            repeating(1)
            while(true){
                MovementListener.currentServerTick++
                yield()
            }
        }
    }
}