package com.derongan.minecraft.deeperworld.ecs

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.geary.minecraft.access.geary
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object SectionChangeListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerAscendEvent.ascend() {
        geary(player).set(DeeperWorldSection(fromSection.key))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerDescendEvent.descend() {
        geary(player).set(DeeperWorldSection(fromSection.key))
    }
}