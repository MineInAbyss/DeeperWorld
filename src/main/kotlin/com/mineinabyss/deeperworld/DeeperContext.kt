package com.mineinabyss.deeperworld

/**
 * Easy access to information related to the [DeeperWorldPlugin] plugin.
 */
object DeeperContext {
    val isBlockLockerLoaded: Boolean = deeperWorld.server.pluginManager.isPluginEnabled("BlockLocker")
    val isGearyLoaded: Boolean = deeperWorld.server.pluginManager.isPluginEnabled("Geary")
    val isFAWELoaded: Boolean = deeperWorld.server.pluginManager.isPluginEnabled("FastAsyncWorldEdit")
}
