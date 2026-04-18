package com.mineinabyss.deeperworld.movement

import com.mineinabyss.deeperworld.DeeperWorldConfig
import com.mineinabyss.deeperworld.sections.SectionFeature
import com.mineinabyss.deeperworld.movement.teleport.BedrockBlockingInvalidTeleportHandler
import com.mineinabyss.deeperworld.movement.teleport.TeleportHandler
import com.mineinabyss.deeperworld.movement.teleport.TransitionTeleportHandler
import com.mineinabyss.deeperworld.movement.teleport.UndoMovementInvalidTeleportHandler
import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.idofront.features.listeners

/**
 * Handles teleporting players and entities between sections.
 */
val SectionTeleportFeature = module("teleport") {
    singleModule(SectionFeature)

    val config = get<DeeperWorldConfig>()
    single<TeleportHandler> { TransitionTeleportHandler() }
    single<OutOfSectionAction> { ApplyDamageOutOfSection(config) }
    val invalidDescentHandler = when {
        config.bedrockBlockingInvalidTeleport -> BedrockBlockingInvalidTeleportHandler()
        else -> UndoMovementInvalidTeleportHandler
    }
    val movementHandler by single {
        MovementHandler(
            get(), get(), get(),
            invalidAscentHandler = UndoMovementInvalidTeleportHandler,
            invalidDescentHandler = invalidDescentHandler
        )
    }

    val movementListener = new(::MovementListener)

    listeners(
        movementListener,
        new(::PlayerListener),
    )

    addCloseable(movementHandler)
}