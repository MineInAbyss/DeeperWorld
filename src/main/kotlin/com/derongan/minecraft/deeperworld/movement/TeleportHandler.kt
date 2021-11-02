package com.derongan.minecraft.deeperworld.movement

interface TeleportHandler {
    fun handleTeleport()
    fun isValidTeleport() : Boolean
}