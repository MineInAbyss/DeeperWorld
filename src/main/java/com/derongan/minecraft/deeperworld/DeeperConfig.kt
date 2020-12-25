package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.world.section.Section
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.config.ReloadScope
import com.mineinabyss.idofront.serialization.WorldSerializer
import kotlinx.serialization.Serializable
import org.bukkit.World

object DeeperConfig : IdofrontConfig<DeeperConfig.Data>(deeperWorld, Data.serializer()) {
    @Serializable
    data class Data(
            val sections: List<Section>,
            val damageOutsideSections: Double = 0.0,
            val damageExcludedWorlds: Set<@Serializable(with = WorldSerializer::class) World> = emptySet(),
            val maxSafeFallingDistance: Float = Float.MAX_VALUE,
            val fallingDamageMultiplier: Double = 0.0,
    )

    init {
        load()
    }

    //TODO add a load() function in idofront
    fun load() {
        data.sections.forEachIndexed { i, section ->
            data.sections.getOrNull(i - 1)?.let { prevSection ->
                section.aboveKey = prevSection.key
                prevSection.belowKey = section.key
            }
            WorldManager.registerSection(section.key, section) //TODO do we need to pass both section key and section?
        }
    }

    override fun reload(): ReloadScope.() -> Unit = {
        attempt(success = "Registered all sections with DeeperWorld",
                fail = "Failed to register sections with DeeperWorld") {
            load()
        }
    }
}