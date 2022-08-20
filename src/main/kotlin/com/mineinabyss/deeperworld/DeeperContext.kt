package com.mineinabyss.deeperworld

/**
 * Easy access to information related to the [DeeperWorldPlugin] plugin.
 */
object DeeperContext {
    val isGearyLoaded: Boolean = deeperWorld.server.pluginManager.isPluginEnabled("Geary")
    val isBlockLockerLoaded: Boolean = deeperWorld.server.pluginManager.isPluginEnabled("BlockLocker")
    val isFAWELoaded: Boolean = deeperWorld.server.pluginManager.isPluginEnabled("FastAsyncWorldEdit")
}
