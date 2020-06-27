package com.derongan.minecraft.deeperworld.synchronization

import com.derongan.minecraft.deeperworld.world.section.correspondingLocation
import com.derongan.minecraft.deeperworld.world.section.inSectionOverlap
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import nl.rutgerkok.blocklocker.BlockLockerPlugin
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

internal val blockLocker: BlockLockerPlugin by lazy { BlockLockerAPIv2.getPlugin() }

internal val copyBlockData = { original: Block, corresponding: Block ->
    corresponding.blockData = original.blockData.clone()
}

internal fun updateMaterial(material: Material) = { _: Block, corr: Block -> corr.type = material }

internal fun updateCorrespondingBlock(original: Location, updater: (original: Block, corresponding: Block) -> Unit) {
    val corresponding: Location = original.correspondingLocation ?: return
    //ensure blocks don't get altered when we are outside of the corresponding region
    if (corresponding.inSectionOverlap)
        updater(original.block, corresponding.block)
}

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

internal fun List<ItemStack?>.dropItems(loc: Location, noVelocity: Boolean) {
    val spawnLoc = loc.clone().add(0.5, if (noVelocity) 1.0 else 0.0, 0.5)
    filterNotNull().forEach { loc.world?.dropItem(spawnLoc, it).apply { if (noVelocity) this?.velocity = Vector(0, 0, 0) } }
}