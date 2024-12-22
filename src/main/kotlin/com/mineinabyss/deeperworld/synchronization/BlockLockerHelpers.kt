package com.mineinabyss.deeperworld.synchronization

import com.mineinabyss.idofront.plugin.Plugins
import nl.rutgerkok.blocklocker.BlockLockerPlugin
import nl.rutgerkok.blocklocker.SearchMode
import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl
import org.bukkit.Material
import org.bukkit.block.Block

internal class BlockLockerHelpers {
    val plugin: BlockLockerPlugin? by lazy { Plugins.getOrNull<BlockLockerPluginImpl>() }

    fun syncBlockLocker(corr: Block) {
        plugin?.protectionFinder?.findProtection(corr, SearchMode.ALL)?.ifPresent {
            it.signs.forEach { linkedSign -> linkedSign.location.block.type = Material.AIR }
        }
    }

    fun updateProtection(block: Block) =
        plugin?.protectionFinder?.findProtection(block, SearchMode.ALL)?.ifPresent {
            it.signs.forEach { sign -> sign.location.sync(signUpdater()) }
        }

}

internal val blockLocker by lazy { if (Plugins.isEnabled("BlockLocker")) BlockLockerHelpers() else null }
