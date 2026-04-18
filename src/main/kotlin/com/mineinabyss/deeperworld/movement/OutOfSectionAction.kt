package com.mineinabyss.deeperworld.movement

import org.bukkit.entity.Player

fun interface OutOfSectionAction {
    fun handleOutOfSection(player: Player)
}