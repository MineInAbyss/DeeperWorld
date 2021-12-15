@file:UseSerializers(DurationSerializer::class)

package com.mineinabyss.deeperworld.config

import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.config.ReloadScope
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.WorldSerializer
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.World
import kotlin.time.Duration

object DeeperConfig : IdofrontConfig<DeeperConfig.Data>(deeperWorld, Data.serializer()) {
    @Serializable
    data class Data(
        val sections: List<Section>,
        val damageOutsideSections: Double = 0.0,
        val damageExcludedWorlds: Set<@Serializable(with = WorldSerializer::class) World> = emptySet(),
        val remountPacketDelay: Duration = 40.ticks,
        val fall: FallDamageConfig = FallDamageConfig(),
        val time: TimeConfig = TimeConfig(),
    ) {
        val worlds = sections.map { it.world }.toSet()
    }

    override fun ReloadScope.load() {
        "Registering all sections with DeeperWorld" {
            data.sections.forEachIndexed { i, section ->
                data.sections.getOrNull(i - 1)?.let { prevSection ->
                    section.aboveKey = prevSection.key
                    prevSection.belowKey = section.key
                }
                WorldManager.registerSection(
                    section.key,
                    section
                ) //TODO do we need to pass both section key and section?
            }
        }
    }
}
