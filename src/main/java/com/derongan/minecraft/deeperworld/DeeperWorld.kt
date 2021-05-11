package com.derongan.minecraft.deeperworld

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.derongan.minecraft.deeperworld.MinecraftConstants.FULL_DAY_TIME
import com.derongan.minecraft.deeperworld.config.DeeperConfig
import com.derongan.minecraft.deeperworld.listeners.MovementListener
import com.derongan.minecraft.deeperworld.listeners.PlayerListener
import com.derongan.minecraft.deeperworld.player.FallingDamageManager
import com.derongan.minecraft.deeperworld.player.PlayerManagerImpl
import com.derongan.minecraft.deeperworld.services.PlayerManager
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.synchronization.ContainerSyncListener
import com.derongan.minecraft.deeperworld.synchronization.ExploitPreventionListener
import com.derongan.minecraft.deeperworld.synchronization.SectionSyncListener
import com.derongan.minecraft.deeperworld.world.WorldManagerImpl
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.okkero.skedule.schedule
import org.bukkit.plugin.java.JavaPlugin

val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

class DeeperWorld : JavaPlugin() {
    @ExperimentalCommandDSL
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
        DeeperCommandExecutor

        // Initialize falling damage task
        if (DeeperConfig.data.fall.maxSafeDist >= 0f && DeeperConfig.data.fall.fallDistanceDamageScaler >= 0.0) {
            schedule {
                repeating(DeeperConfig.data.fall.hitDelay.inTicks.coerceAtLeast(1))
                while (true) {
                    server.onlinePlayers.forEach {
                        FallingDamageManager.updateFallingDamage(it)
                    }
                    yield()
                }
            }
        }

        // Initialize time synchronization task
        if (DeeperConfig.data.time.syncedWorlds.isNotEmpty()) {
            DeeperConfig.data.time.mainWorld?.let { mainWorld ->
                schedule {
                    repeating(DeeperConfig.data.time.updateInterval.inTicks)
                    while (true) {
                        val mainWorldTime = mainWorld.time
                        DeeperConfig.data.time.syncedWorlds.forEach { (world, offset) ->
                            world.time = (mainWorldTime + offset) % FULL_DAY_TIME
                        }
                        yield()
                    }
                }
            }
        }
    }
}
