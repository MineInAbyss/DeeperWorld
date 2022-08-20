package com.mineinabyss.deeperworld

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.derongan.minecraft.deeperworld.ecs.SectionChangeListener
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.mineinabyss.deeperworld.config.DeeperConfig
import com.mineinabyss.deeperworld.config.deeperConfig
import com.mineinabyss.deeperworld.listeners.MovementListener
import com.mineinabyss.deeperworld.listeners.PlayerListener
import com.mineinabyss.deeperworld.player.FallingDamageManager
import com.mineinabyss.deeperworld.player.PlayerManagerImpl
import com.mineinabyss.deeperworld.services.PlayerManager
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.deeperworld.synchronization.ContainerSyncListener
import com.mineinabyss.deeperworld.synchronization.ExploitPreventionListener
import com.mineinabyss.deeperworld.synchronization.SectionSyncListener
import com.mineinabyss.deeperworld.world.WorldManagerImpl
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

class DeeperWorldPlugin : JavaPlugin() {
    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        saveDefaultConfig()
        registerService<WorldManager>(WorldManagerImpl(config))

        DeeperConfig.load()

        registerService<PlayerManager>(PlayerManagerImpl())
        registerEvents(
            MovementListener,
            PlayerListener,
            SectionSyncListener,
            ExploitPreventionListener,
            ContainerSyncListener
        )

        if (DeeperContext.isGearyLoaded)
            registerEvents(SectionChangeListener)

        //register command executor
        DeeperCommandExecutor()

        // Initialize falling damage task
        if (deeperConfig.fall.maxSafeDist >= 0f && deeperConfig.fall.fallDistanceDamageScaler >= 0.0) {
            val hitDelay = deeperConfig.fall.hitDelay.coerceAtLeast(1.ticks)
            deeperWorld.launch {
                while (true) {
                    server.onlinePlayers.forEach(FallingDamageManager::updateFallingDamage)
                    delay(hitDelay)
                }
            }
        }

        // Initialize time synchronization task
        if (deeperConfig.time.syncedWorlds.isNotEmpty()) {
            deeperConfig.time.mainWorld?.let { mainWorld ->
                val updateInterval = deeperConfig.time.updateInterval.coerceAtLeast(1.ticks)
                deeperWorld.launch {
                    while (true) {
                        val mainWorldTime = mainWorld.time
                        deeperConfig.time.syncedWorlds.forEach { (world, offset) ->
                            world.time = (mainWorldTime + offset) % FULL_DAY_TIME
                        }
                        delay(updateInterval)
                    }
                }
            }
        }
    }

    override fun onDisable() {
        MovementListener.temporaryBedrock.forEach {
            it.type = Material.AIR
        }
    }
}
