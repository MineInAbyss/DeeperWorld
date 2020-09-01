package com.derongan.minecraft.deeperworld

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin

/**
 * Gets [DeeperWorld] via Bukkit once, then sends that reference back afterwards
 */
val deeperWorld: DeeperWorld by lazy { JavaPlugin.getPlugin(DeeperWorld::class.java) }

//TODO move into idofront
inline fun <reified T> Plugin.registerService(impl: T, priority: ServicePriority = ServicePriority.Lowest) =
        server.servicesManager.register(T::class.java, impl, this, priority)

fun Plugin.registerEvents(vararg listeners: Listener) =
        listeners.forEach { server.pluginManager.registerEvents(it, this) }

inline fun <reified T> getService() = Bukkit.getServer().servicesManager.load(T::class.java)!!
inline fun <reified T : Plugin> getPlugin() = Bukkit.getPluginManager().plugins.first { it is T }

fun Event.call() = Bukkit.getServer().pluginManager.callEvent(this)
inline fun Event.call(onSuccess: Event.() -> Unit) {
    Bukkit.getServer().pluginManager.callEvent(this)
    if(this !is Cancellable || !isCancelled)
        onSuccess()
}

operator fun PlayerEvent.component1() = player