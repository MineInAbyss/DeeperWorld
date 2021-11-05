package com.derongan.minecraft.deeperworld.movement.transition

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerChangeSectionEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.world.section.Section
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
