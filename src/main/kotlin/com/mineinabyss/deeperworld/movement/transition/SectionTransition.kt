package com.mineinabyss.deeperworld.movement.transition

import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerChangeSectionEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.deeperworld.world.section.Section
import org.bukkit.Location
import org.bukkit.entity.Player

data class SectionTransition(
    val from: Location,
    val to: Location,
    val fromSection: Section,
    val toSection: Section,
    val kind: TransitionKind,
    val teleportUnnecessary: Boolean,
)

enum class TransitionKind {
    ASCEND,
    DESCEND
}

internal fun SectionTransition.toEvent(player: Player): PlayerChangeSectionEvent {
    return if (this.kind == TransitionKind.ASCEND) {
        PlayerAscendEvent(player, fromSection, toSection)
    } else {
        PlayerDescendEvent(player, fromSection, toSection)
    }
}
