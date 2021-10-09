package com.derongan.minecraft.deeperworld

import org.bukkit.plugin.java.JavaPlugin

/**
 * Gets [DeeperWorldPlugin] via Bukkit once, then sends that reference back afterwards
 */
val deeperWorld: DeeperWorldPlugin by lazy { JavaPlugin.getPlugin(DeeperWorldPlugin::class.java) }
