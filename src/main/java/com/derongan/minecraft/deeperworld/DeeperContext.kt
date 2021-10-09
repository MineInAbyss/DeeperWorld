package com.derongan.minecraft.deeperworld

/**
 * Easy access to information related to the [DeeperWorldPlugin] plugin.
 */
object DeeperContext {
    val isBlockLockerLoaded: Boolean = deeperWorld.server.pluginManager.isPluginEnabled("BlockLocker")
    val isFAWELoaded: Boolean = deeperWorld.server.pluginManager.isPluginEnabled("FastAsyncWorldEdit")
}
