package com.mineinabyss.deeperworld

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.mineinabyss.deeperworld.config.DeeperConfig
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

        //register command executor
        DeeperCommandExecutor()

        // Initialize falling damage task
        if (DeeperConfig.data.fall.maxSafeDist >= 0f && DeeperConfig.data.fall.fallDistanceDamageScaler >= 0.0) {
            val hitDellay = DeeperConfig.data.fall.hitDelay.coerceAtLeast(1.ticks)
            deeperWorld.launch {
                while (true) {
                    server.onlinePlayers.forEach {
                        FallingDamageManager.updateFallingDamage(it)
                    }
                    delay(hitDellay)
                }
            }
        }

        // Initialize time synchronization task
        if (DeeperConfig.data.time.syncedWorlds.isNotEmpty()) {
            DeeperConfig.data.time.mainWorld?.let { mainWorld ->
                val updateInterval = DeeperConfig.data.time.updateInterval.coerceAtLeast(1.ticks)
                deeperWorld.launch {
                    while (true) {
                        val mainWorldTime = mainWorld.time
                        DeeperConfig.data.time.syncedWorlds.forEach { (world, offset) ->
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
