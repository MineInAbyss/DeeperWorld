package com.mineinabyss.deeperworld.synchronization

import com.mineinabyss.deeperworld.sections.SectionRepository
import com.mineinabyss.idofront.plugin.Plugins
import nl.rutgerkok.blocklocker.BlockLockerPlugin
import nl.rutgerkok.blocklocker.SearchMode
import nl.rutgerkok.blocklocker.impl.BlockLockerPluginImpl
import org.bukkit.Material
import org.bukkit.block.Block

class BlockLockerHelpers(
    val sections: SectionRepository,
) {
    val plugin: BlockLockerPlugin? by lazy { Plugins.getOrNull<BlockLockerPluginImpl>() }

    fun syncBlockLocker(corr: Block) {
        plugin?.protectionFinder?.findProtection(corr, SearchMode.ALL)?.ifPresent {
            it.signs.forEach { linkedSign -> linkedSign.location.block.type = Material.AIR }
        }
    }

    fun updateProtection(block: Block) {
        plugin?.protectionFinder?.findProtection(block, SearchMode.ALL)?.ifPresent {
            it.signs.forEach { sign ->
                val block = sign.location.block
                sections.whenLinked(block) { linked ->
                    updateSign(linked, block)
                }
            }
        }
    }

}
