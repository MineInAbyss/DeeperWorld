package com.mineinabyss.deeperworld

import com.mineinabyss.deeperworld.sections.SectionFeature
import com.mineinabyss.deeperworld.player.PlayerRepository
import com.mineinabyss.deeperworld.sections.SectionRepository
import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.scope
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.plugin.Plugin

/**
 * Easy access to information related to the [DeeperWorldPlugin] plugin.
 */
interface DeeperContext: Plugin, DI {
    val logger: ComponentLogger
    val config: DeeperWorldConfig

    val players: PlayerRepository get() = scope[SectionFeature].get()
    val sections: SectionRepository get() =  scope[SectionFeature].get()

    companion object {
        var instance: DeeperContext? = null
    }
}

val deeperWorld get() = DeeperContext.instance ?: error("DeeperWorld not loaded yet!")