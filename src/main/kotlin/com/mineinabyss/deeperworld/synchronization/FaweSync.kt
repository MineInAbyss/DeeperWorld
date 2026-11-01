package com.mineinabyss.deeperworld.synchronization

import com.fastasyncworldedit.core.util.TaskManager
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.World

object FaweSync {
    fun copy(region: SyncRegion, onComplete: (Throwable?) -> Unit) = with(region) {
        val cuboid = CuboidRegion(BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ))
        val clipboard = BlockArrayClipboard(cuboid)
        val adapter = WorldEditPlugin.getInstance().bukkitImplAdapter
        val newEditSession = { world: World ->
            WorldEdit.getInstance().newEditSessionBuilder().world(world).limitUnlimited().build()
        }

        TaskManager.taskManager().taskNowAsync {
            onComplete(runCatching {
                newEditSession(adapter.adapt(source)).use { copySession ->
                    val copy = ForwardExtentCopy(copySession, cuboid, clipboard, cuboid.minimumPoint)
                    copy.isCopyingEntities = false
                    copy.isCopyingBiomes = true
                    Operations.complete(copy)
                }
                newEditSession(adapter.adapt(target)).use { pasteSession ->
                    Operations.complete(
                        ClipboardHolder(clipboard).createPaste(pasteSession)
                            .to(BlockVector3.at(minX + dx, minY + dy, minZ + dz))
                            .build()
                    )
                }
            }.exceptionOrNull())
        }
    }
}
