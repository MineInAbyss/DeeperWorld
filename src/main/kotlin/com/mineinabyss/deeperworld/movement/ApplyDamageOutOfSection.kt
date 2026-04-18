package com.mineinabyss.deeperworld.movement

import com.mineinabyss.deeperworld.DeeperWorldConfig
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class ApplyDamageOutOfSection(
    val config: DeeperWorldConfig
) : OutOfSectionAction {
    override fun handleOutOfSection(player: Player) = with(player) {
        if (config.damageOutsideSections > 0.0
            && location.world !in config.damageExcludedWorlds
            && (gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE)
            && location.world in config.worlds
        ) {
            damage(0.01) //give a damage effect
            health = (health - config.damageOutsideSections / 10)
                .coerceIn(0.0, getAttribute(Attribute.MAX_HEALTH)?.value) //ignores armor
            showTitle(
                Title.title(
                    "You are not in a managed section".miniMsg().color(NamedTextColor.RED),
                    "You will take damage upon moving!".miniMsg().color(NamedTextColor.GRAY),
                    Title.Times.times(
                        0.seconds.toJavaDuration(),
                        1.seconds.toJavaDuration(),
                        0.5.seconds.toJavaDuration()
                    )
                )
            )
        }
    }
}