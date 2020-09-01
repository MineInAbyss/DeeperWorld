package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.listeners.PlayerListener
import com.derongan.minecraft.deeperworld.player.PlayerManager
import com.derongan.minecraft.deeperworld.synchronization.SectionSyncListener
import com.derongan.minecraft.deeperworld.world.WorldManager
import com.derongan.minecraft.deeperworld.world.WorldManagerImpl
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class DeeperWorld : JavaPlugin() {
    private lateinit var worldManager: WorldManagerImpl
    @ExperimentalCommandDSL
    override fun onEnable() {
        createConfig()

        //TODO convert PlayerManager, WorldManager, and MovementListener to singleton objects
        worldManager = WorldManagerImpl(config)

        server.servicesManager.register(WorldManager::class.java, worldManager, this, ServicePriority.Lowest)
        server.pluginManager.registerEvents(MovementListener(PlayerManager), this)
        server.pluginManager.registerEvents(PlayerListener, this)
        server.pluginManager.registerEvents(SectionSyncListener, this)

        //register command executor
        DeeperCommandExecutor(worldManager)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun createConfig() {
        try {
            if (!dataFolder.exists()) {
                if (!dataFolder.mkdirs()) {
                    throw RuntimeException("Failed to make config file")
                }
            }
            val file = File(dataFolder, "config.yml")
            if (!file.exists()) {
                logger.info("Config.yml not found, creating!")
                saveDefaultConfig()
            } else {
                logger.info("Config.yml found, loading!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}