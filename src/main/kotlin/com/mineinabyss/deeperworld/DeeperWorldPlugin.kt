package com.mineinabyss.deeperworld

import com.mineinabyss.deeperworld.player.FallDamageFeature
import com.mineinabyss.deeperworld.player.PlayerRepository
import com.mineinabyss.deeperworld.sections.SectionRepository
import com.mineinabyss.deeperworld.sections.SectionFeature
import com.mineinabyss.deeperworld.synchronization.SectionSyncFeature
import com.mineinabyss.deeperworld.movement.SectionTeleportFeature
import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.DIContext
import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.getLazy
import com.mineinabyss.dependencies.loadAllCatching
import com.mineinabyss.dependencies.loadCatching
import com.mineinabyss.dependencies.scope
import com.mineinabyss.dependencies.single
import com.mineinabyss.idofront.config.SingleConfig
import com.mineinabyss.idofront.features.MainCommand
import com.mineinabyss.idofront.features.MainCommandFeature
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.features.singlePluginLogger
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class DeeperWorldPlugin : JavaPlugin(), DeeperContext {
    override val di: DIContext = DI {
        single<Plugin> { this@DeeperWorldPlugin }
        singlePluginLogger(this@DeeperWorldPlugin)
        singleConfig<DeeperWorldConfig>("config.yml")
        single {
            MainCommand(
                names = listOf("deeperworld", "dw"),
                description = "Main command for DeeperWorld",
                reloadCommandName = "reload",
                onBeforeReload = {
                    get<SingleConfig<DeeperWorldConfig>>().updateCached()
                }
            )
        }
    }
    override val logger: ComponentLogger by getLazy()
    override val config: DeeperWorldConfig by getLazy()

    @Volatile private var cachedPlayers: PlayerRepository? = null
    @Volatile private var cachedSections: SectionRepository? = null

    override val players: PlayerRepository
        get() = cachedPlayers ?: sectionFeatureValue<PlayerRepository> { cachedPlayers = it }

    override val sections: SectionRepository
        get() = cachedSections ?: sectionFeatureValue<SectionRepository> { cachedSections = it }

    // DIScope is not thread safe and reloads run on the main thread, so only cache there to never keep a value from an unloaded module
    private inline fun <reified T : Any> sectionFeatureValue(crossinline cache: (T?) -> Unit): T {
        val feature = scope[SectionFeature]
        val value = feature.get<T>()
        if (Bukkit.isPrimaryThread()) {
            cache(value)
            feature.addCloseable { cache(null) }
        }
        return value
    }

    override fun onLoad() {
        DeeperContext.instance = this@DeeperWorldPlugin
    }

    override fun onEnable() {
        scope.loadAllCatching(
            SectionFeature,
            SectionTeleportFeature,
            SectionSyncFeature,
            FallDamageFeature,
        )

        scope.loadCatching(MainCommandFeature)
    }

    override fun reloadConfig() {
        get<SingleConfig<DeeperWorldConfig>>().updateCached()
    }
}
