package com.derongan.minecraft.deeperworld.synchronization

import com.derongan.minecraft.deeperworld.world.section.*
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import nl.rutgerkok.blocklocker.BlockLockerPlugin
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

internal val blockLocker: BlockLockerPlugin by lazy { BlockLockerAPIv2.getPlugin() }

internal fun copyBlockData(original: Block, corresponding: Block) {
    corresponding.blockData = original.blockData.clone()
}

internal fun updateMaterial(material: Material) = { _: Block, corr: Block -> corr.type = material }

internal inline fun Block.sync(updater: (original: Block, corresponding: Block) -> Unit = ::copyBlockData) =
    location.sync(updater)

internal inline fun Location.sync(
    updater: (original: Block, corresponding: Block, section: Section, corrSection: Section) -> Unit
) {
    if (!inSectionOverlap) return //ensure blocks don't get altered when we are outside of the corresponding region
    val section = section ?: return
    val correspondingSection = correspondingSection ?: return
    val corresponding = getCorrespondingLocation(section, correspondingSection) ?: return
    updater(block, corresponding.block, section, correspondingSection)
}

internal inline fun Location.sync(updater: (original: Block, corresponding: Block) -> Unit = ::copyBlockData) =
    sync { original, corresponding, _, _ -> updater(original, corresponding) }

internal fun signUpdater(lines: Array<String>? = null) = { original: Block, corresponding: Block ->
    copyBlockData(original, corresponding)
    val sign = original.state
    if (sign is Sign) {
        val readLines = lines ?: sign.lines
        val corrSign = corresponding.state
        if (corrSign is Sign && !corrSign.lines.contentEquals(readLines)) {
            readLines.forEachIndexed { i, line -> corrSign.setLine(i, line) }
            corrSign.update()
        }
    }
}

internal fun Collection<ItemStack?>.dropItems(loc: Location, noVelocity: Boolean) {
    val spawnLoc = loc.clone().add(0.5, if (noVelocity) 1.0 else 0.0, 0.5)
    filterNotNull().forEach {
        loc.world?.dropItem(spawnLoc, it).apply { if (noVelocity) this?.velocity = Vector(0, 0, 0) }
    }
}
