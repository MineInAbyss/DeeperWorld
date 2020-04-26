package com.derongan.minecraft.deeperworld

/**
 * Easy access to information related to the [DeeperWorld] plugin.
 */
object DeeperContext {
    val config = deeperWorld.config
    val preventTeleportInUnmanaged =
        if (config.contains("prevent-teleport-in-unmanaged"))
                config.getBoolean("prevent-teleport-in-unmanaged")
        else true
    val damagePlayersOutsideSections =
            if (config.contains("damage-outside-sections"))
                config.getDouble("damage-outside-sections")
            else 0.0
    val isBlockLockerLoaded = deeperWorld.server.pluginManager.isPluginEnabled("BlockLocker")
}