package com.derongan.minecraft.deeperworld

/**
 * Easy access to information related to the [DeeperWorld] plugin.
 */
object DeeperContext {
    val isBlockLockerLoaded = deeperWorld.server.pluginManager.isPluginEnabled("BlockLocker")
}