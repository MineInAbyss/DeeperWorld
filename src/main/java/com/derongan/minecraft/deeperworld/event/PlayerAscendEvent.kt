package com.derongan.minecraft.deeperworld.event

import com.derongan.minecraft.deeperworld.world.section.Section
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class PlayerAscendEvent(
    player: Player,
    fromSection: Section,
    toSection: Section
) : PlayerChangeSectionEvent(player, fromSection, toSection) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}