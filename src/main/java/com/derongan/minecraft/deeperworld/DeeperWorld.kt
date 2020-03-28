package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.player.PlayerManager
import com.derongan.minecraft.deeperworld.synchronization.SectionSyncListener
import com.derongan.minecraft.deeperworld.world.WorldManager
import com.derongan.minecraft.deeperworld.world.WorldManagerImpl
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * Gets [DeeperWorld] via Bukkit once, then sends that reference back afterwards
 */
val deeperWorld: DeeperWorld by lazy { JavaPlugin.getPlugin(DeeperWorld::class.java) }

class DeeperWorld : JavaPlugin() {
    override fun onEnable() {
        createConfig()
        val playerManager = PlayerManager()
        val worldManager = WorldManagerImpl(config)
        server.servicesManager.register(WorldManager::class.java, worldManager, this, ServicePriority.Lowest)

        //register listeners
        //TODO if it's okay to give outside classes access to playerManager and worldManager convert command executor,
        // and movement listener to objects, and convert PlayerManager, and WorldManagerImpl into objects themselves.
        val movementListener = MovementListener(playerManager)
        server.pluginManager.registerEvents(movementListener, this)
        server.pluginManager.registerEvents(SectionSyncListener, this)

        //register commands TODO rewrite with Idofront once ready
        val commandExecutor = DeeperCommandExecutor(playerManager, worldManager)
        getCommand("sectionon")!!.setExecutor(commandExecutor)
        getCommand("sectionoff")!!.setExecutor(commandExecutor)
        getCommand("linfo")!!.setExecutor(commandExecutor)
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