@file:UseSerializers(DurationSerializer::class)

package com.mineinabyss.deeperworld

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.deeperworld.datastructures.CubePoint
import com.mineinabyss.deeperworld.datastructures.Region
import com.mineinabyss.deeperworld.datastructures.SectionConfig
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.WorldSerializer
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.Bukkit
import org.bukkit.World
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
data class DeeperWorldConfig(
    val sections: List<SectionConfig> = listOf(
        SectionConfig("section1", Region(0, 0, 0, 1000, 256, 1000), Bukkit.getWorld("world")!!, CubePoint(0, 0, 0), CubePoint(0, 16, 0)),
        SectionConfig("section2", Region(1000, 0, 0, 2000, 256, 1000), Bukkit.getWorld("world")!!, CubePoint(1000, 240, 0), CubePoint(2000, 16, 0)),
    ),
    @YamlComment("The damage players will take when outside a managed section.")
    val damageOutsideSections: Double = 1.0,
    @YamlComment("Worlds which shouldn't damage players when outside of a section.")
    val damageExcludedWorlds: Set<@Serializable(with = WorldSerializer::class) World> = emptySet(),
    val fall: FallDamageConfig = FallDamageConfig(),
    val time: TimeConfig = TimeConfig(),
    val bedrockBlockingInvalidTeleport: Boolean = true,
) {
    val worlds = sections.map { it.world }.toSet()

    @Serializable
    data class FallDamageConfig(
        @YamlComment("Whether to deal damage when falling long distances.")
        val enabled: Boolean = true,
        @YamlComment("The maximum safe falling distance, after which players will start taking damage. Set to -1 to disable falling damage")
        val maxSafeDist: Float = -1f,
        @YamlComment("The multiplier for damage taken while falling. Set to 0 to deal consistent damage.")
        val fallDistanceDamageScaler: Double = 0.01,
        @YamlComment("The minimum damage to deal.")
        val startingDamage: Double = 1.0,
        @YamlComment("How often to damage players in ticks")
        val hitDelay: @Serializable(DurationSerializer::class) Duration = 10.ticks,
        @YamlComment("Whether to spawn cloud particles when the player is being damaged.")
        val spawnParticles: Boolean = true,
    )

    @Serializable
    data class TimeConfig(
        @YamlComment("How often to synchronize time between worlds")
        val updateInterval: @Serializable(DurationSerializer::class) Duration = 1800.seconds,
        @YamlComment("The main synchronization world. Other worlds will get synchronized based on the time in this world.")
        val mainWorld: @Serializable(WorldSerializer::class) World? = Bukkit.getWorld("world"),
        @YamlComment("The worlds where time should be synchronized with the mainWorld. Optionally specify a time offset (leave 0 if no offset is desired)")
        val syncedWorlds: Map<@Serializable(WorldSerializer::class) World, Long> = mutableMapOf(Bukkit.getWorld("world")!! to 0L),
    )
}
