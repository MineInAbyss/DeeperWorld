package com.mineinabyss.deeperworld.synchronization

import com.mineinabyss.deeperworld.sections.correspondingLocation
import org.bukkit.Location
import org.bukkit.World
import kotlin.math.abs

/**
 * Inclusive block bounds in [source], copied to [target] shifted by ([dx], [dy], [dz]).
 * The y range is clamped so both ends stay within their world's build height
 */
data class SyncRegion(
    val source: World,
    val target: World,
    val minX: Int, val minY: Int, val minZ: Int,
    val maxX: Int, val maxY: Int, val maxZ: Int,
    val dx: Int, val dy: Int, val dz: Int,
) {
    val volume: Long get() = (maxX - minX + 1).toLong() * (maxY - minY + 1) * (maxZ - minZ + 1)

    val overlapsItself: Boolean
        get() = source == target && abs(dx) <= maxX - minX && abs(dy) <= maxY - minY && abs(dz) <= maxZ - minZ

    companion object {
        fun around(location: Location, range: Int): SyncRegion? {
            val linked = location.correspondingLocation?.block ?: return null
            val origin = location.block
            val source = origin.world
            val target = linked.world
            val dy = linked.y - origin.y
            val minY = maxOf(origin.y - range, source.minHeight, target.minHeight - dy)
            val maxY = minOf(origin.y + range, source.maxHeight - 1, target.maxHeight - 1 - dy)
            if (minY > maxY) return null
            return SyncRegion(
                source, target,
                origin.x - range, minY, origin.z - range,
                origin.x + range, maxY, origin.z + range,
                linked.x - origin.x, dy, linked.z - origin.z,
            )
        }
    }
}
