package com.derongan.minecraft.deeperworld

import org.bukkit.plugin.java.JavaPlugin

/**
 * Gets [DeeperWorld] via Bukkit once, then sends that reference back afterwards
 */
val deeperWorld: DeeperWorld by lazy { JavaPlugin.getPlugin(DeeperWorld::class.java) }
