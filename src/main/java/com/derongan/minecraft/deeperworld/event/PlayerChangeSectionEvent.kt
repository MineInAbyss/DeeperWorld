package com.derongan.minecraft.deeperworld.event

import com.derongan.minecraft.deeperworld.world.section.Section
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

abstract class PlayerChangeSectionEvent(
    player: Player,
    val fromSection: Section,
    val toSection: Section
) : PlayerEvent(player), Cancellable {
    private var cancelled = false

    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}