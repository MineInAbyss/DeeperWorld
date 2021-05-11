package com.derongan.minecraft.deeperworld.config

import com.derongan.minecraft.deeperworld.deeperWorld
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.world.section.Section
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.config.ReloadScope
import com.mineinabyss.idofront.serialization.WorldSerializer
import com.mineinabyss.idofront.time.TimeSpan
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.Serializable
import org.bukkit.World

object DeeperConfig : IdofrontConfig<DeeperConfig.Data>(deeperWorld, Data.serializer()) {
    @Serializable
    data class Data(
        val sections: List<Section>,
        val damageOutsideSections: Double = 0.0,
        val damageExcludedWorlds: Set<@Serializable(with = WorldSerializer::class) World> = emptySet(),
        val remountPacketDelay: TimeSpan = 40.ticks,
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
                WorldManager.registerSection(section.key, section) //TODO do we need to pass both section key and section?
            }
        }
    }
}
