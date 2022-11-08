package com.mineinabyss.deeperworld

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.MinecraftConstants.FULL_DAY_TIME
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
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.service
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

class DeeperWorldPlugin : JavaPlugin() {
    lateinit var config: IdofrontConfig<DeeperWorldConfig>
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        config = config("config") { fromPluginPath(loadDefault = true)}

        service<WorldManager>(WorldManagerImpl())
        service<PlayerManager>(PlayerManagerImpl())

        // Register aboveKey / belowKey as new config breaks this
        for (section in deeperConfig.sections) {
            when (section) {
                deeperConfig.sections.first() -> section.belowKey = deeperConfig.sections[1].key
                deeperConfig.sections.last() -> section.aboveKey = deeperConfig.sections[deeperConfig.sections.size - 2].key
                else -> {
                    section.aboveKey = deeperConfig.sections[deeperConfig.sections.indexOf(section) - 1].key
                    section.belowKey = deeperConfig.sections[deeperConfig.sections.indexOf(section) + 1].key
                }
            }
        }

        listeners(
            MovementListener,
            PlayerListener,
            SectionSyncListener,
            ExploitPreventionListener,
            ContainerSyncListener
        )

        //register command executor
        DeeperCommandExecutor()

        // Initialize falling damage task
        if (deeperConfig.fall.maxSafeDist >= 0f && deeperConfig.fall.fallDistanceDamageScaler >= 0.0) {
            val hitDellay = deeperConfig.fall.hitDelay.coerceAtLeast(1.ticks)
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
