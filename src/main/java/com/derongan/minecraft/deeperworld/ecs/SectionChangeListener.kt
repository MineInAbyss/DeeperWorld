package com.derongan.minecraft.deeperworld.ecs

import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.geary.papermc.access.toGeary
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object SectionChangeListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerAscendEvent.ascend() {
        player.toGeary().set(DeeperWorldSection(fromSection.key))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerDescendEvent.descend() {
        player.toGeary().set(DeeperWorldSection(fromSection.key))
    }
}
