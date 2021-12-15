package com.mineinabyss.deeperworld.event

import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent

class BlockSyncEvent(
    block: Block,
    val type: SyncType
) : BlockEvent(block), Cancellable {
    private var cancelled = false

    override fun isCancelled() = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

enum class SyncType {
    PLACE,
    BREAK
}
