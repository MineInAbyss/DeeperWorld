package com.mineinabyss.deeperworld.synchronization

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

//TODO remove all/simplify
internal fun copyBlockData(original: Block, corresponding: Block) {
    corresponding.blockData = original.blockData.clone()
}

internal fun updateSign(corresponding: Block, original: Block, lines: MutableList<Component>? = null) {
    copyBlockData(original, corresponding)
    val sign = original.state
    if (sign is Sign) for (side in Side.entries) {
        val readLines = lines ?: sign.getSide(side).lines()
        val corrSign = corresponding.state
        if (corrSign is Sign && !corrSign.getSide(side).lines().containsAll(readLines)) {
            readLines.forEachIndexed { i, line -> corrSign.getSide(side).line(i, line) }
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
