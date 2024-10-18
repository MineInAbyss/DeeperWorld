package com.mineinabyss.deeperworld

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.mineinabyss.deeperworld.listeners.MovementListener
import com.mineinabyss.deeperworld.listeners.PlayerListener
import com.mineinabyss.deeperworld.nms.coordinate.CoordinateFixer
import com.mineinabyss.deeperworld.player.FallingDamageManager
import com.mineinabyss.deeperworld.player.PlayerManagerImpl
import com.mineinabyss.deeperworld.services.PlayerManager
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.deeperworld.synchronization.ContainerSyncListener
import com.mineinabyss.deeperworld.synchronization.ExploitPreventionListener
import com.mineinabyss.deeperworld.synchronization.SectionSyncListener
import com.mineinabyss.deeperworld.world.WorldManagerImpl
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.service
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

class DeeperWorldPlugin : JavaPlugin() {
    override fun onEnable() {
        createDeeperWorldContext()
        service<WorldManager>(WorldManagerImpl())
        service<PlayerManager>(PlayerManagerImpl())

        // Register aboveKey / belowKey as new config breaks this
        for (section in deeperWorld.config.sections) {
            when (section) {
                deeperWorld.config.sections.first() -> section.belowKey = deeperWorld.config.sections[1].key
                deeperWorld.config.sections.last() -> section.aboveKey = deeperWorld.config.sections[deeperWorld.config.sections.size - 2].key
                else -> {
                    section.aboveKey = deeperWorld.config.sections[deeperWorld.config.sections.indexOf(section) - 1].key
                    section.belowKey = deeperWorld.config.sections[deeperWorld.config.sections.indexOf(section) + 1].key
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

        // Register packet interceptors
        CoordinateFixer.handleIntercept()

        // Initialize falling damage task
        if (deeperWorld.config.fall.maxSafeDist >= 0f && deeperWorld.config.fall.fallDistanceDamageScaler >= 0.0) {
            val hitDellay = deeperWorld.config.fall.hitDelay.coerceAtLeast(1.ticks)
            deeperWorld.plugin.launch {
                while (true) {
                    server.onlinePlayers.forEach {
                        FallingDamageManager.updateFallingDamage(it)
                    }
                    delay(hitDellay)
                }
            }
        }

        // Initialize time synchronization task
        if (deeperWorld.config.time.syncedWorlds.isNotEmpty()) {
            deeperWorld.config.time.mainWorld?.let { mainWorld ->
                val updateInterval = deeperWorld.config.time.updateInterval.coerceAtLeast(1.ticks)
                deeperWorld.plugin.launch {
                    while (true) {
                        val mainWorldTime = mainWorld.time
                        deeperWorld.config.time.syncedWorlds.forEach { (world, offset) ->
                            world.time = (mainWorldTime + offset) % FULL_DAY_TIME
                        }
                        delay(updateInterval)
                    }
                }
            }
        }
    }

    fun createDeeperWorldContext() {
        DI.remove<DeeperContext>()
        DI.add<DeeperContext>(object : DeeperContext {
            override val plugin = this@DeeperWorldPlugin
            override val config: DeeperWorldConfig by config("config", dataFolder.toPath(), DeeperWorldConfig())
        })
    }

    override fun onDisable() {
        MovementListener.temporaryBedrock.forEach {
            it.type = Material.AIR
        }
    }
}
