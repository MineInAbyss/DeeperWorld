package com.mineinabyss.deeperworld.synchronization

import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.bukkit.block.TileState

/**
 * Copies [region] on the main thread one chunk column at a time, spending at most `sync.nativeTimePerTick` per tick.
 * Must be called from the main thread dispatcher
 */
object NativeSync {
    suspend fun copy(region: SyncRegion, onProgress: (copied: Long) -> Unit) = with(region) {
        val budget = deeperWorld.config.sync.nativeTimePerTick.inWholeNanoseconds
        var tickStart = System.nanoTime()
        var copied = 0L

        for (cx in (minX shr 4)..(maxX shr 4)) for (cz in (minZ shr 4)..(maxZ shr 4)) {
            val x0 = maxOf(minX, cx shl 4)
            val x1 = minOf(maxX, (cx shl 4) + 15)
            val z0 = maxOf(minZ, cz shl 4)
            val z1 = minOf(maxZ, (cz shl 4) + 15)

            val chunks = buildList {
                add(source.getChunkAtAsync(cx, cz).await())
                for (tcx in (x0 + dx shr 4)..(x1 + dx shr 4)) for (tcz in (z0 + dz shr 4)..(z1 + dz shr 4))
                    add(target.getChunkAtAsync(tcx, tcz).await())
            }
            chunks.forEach { it.addPluginChunkTicket(deeperWorld) }
            val biomeChangedChunks = mutableSetOf<Pair<Int, Int>>()
            try {
                for (x in x0..x1) for (z in z0..z1) for (y in minY..maxY) {
                    if (copyBlock(x, y, z)) biomeChangedChunks += (x + dx shr 4) to (z + dz shr 4)
                    if (++copied % 256 == 0L && System.nanoTime() - tickStart > budget) {
                        onProgress(copied)
                        delay(1.ticks)
                        tickStart = System.nanoTime()
                    }
                }
            } finally {
                biomeChangedChunks.forEach { (tcx, tcz) -> target.refreshChunk(tcx, tcz) }
                chunks.forEach { it.removePluginChunkTicket(deeperWorld) }
            }
        }
        onProgress(copied)
    }

    /** @return whether the target biome changed, which clients only see once the chunk is resent */
    private fun SyncRegion.copyBlock(x: Int, y: Int, z: Int): Boolean {
        val from = source.getBlockAt(x, y, z)
        val to = target.getBlockAt(x + dx, y + dy, z + dz)

        val state = from.getState(false)
        if (state is TileState) state.copy(to.location).update(true, false)
        else if (to.blockData != from.blockData) to.setBlockData(from.blockData, false)

        val biome = source.getBiome(x, y, z)
        if (target.getBiome(x + dx, y + dy, z + dz) == biome) return false
        target.setBiome(x + dx, y + dy, z + dz, biome)
        return true
    }
}
