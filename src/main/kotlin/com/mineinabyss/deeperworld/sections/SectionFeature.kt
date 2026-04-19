package com.mineinabyss.deeperworld.sections

import com.mineinabyss.deeperworld.DeeperWorldConfig
import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.player.PlayerRepository
import com.mineinabyss.deeperworld.player.PlayerRepositoryImpl
import com.mineinabyss.deeperworld.player.canMoveSections
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.entity.Player

/**
 * Reads sections from config.
 */
val SectionFeature = module("sections") {
    val config = get<DeeperWorldConfig>()
    single<SectionDataSource> { SectionConfigDataSource(config) }
    single<SectionRepository> { new(::SectionRepository) }
    single<PlayerRepository> { new(::PlayerRepositoryImpl) }
}.mainCommand {
    "tp" {
        executes.args("player" to Args.otherPlayer()) { player ->
            val canMoveSections = !player.canMoveSections
            player.canMoveSections = canMoveSections
            val msg = (if (!player.canMoveSections) "<red>" else "").plus(player.name)
            sender.success("Automatic TP handled for $msg")
        }
    }
    ("layerinfo" / "linfo" / "info" / "layers" / "layers") {
        executes.asPlayer {
            val section = player.location.section?.section
            if (section == null) sender.info("${player.name} is not in a managed section")
            else sender.info("${player.name} is in section ${section.key}")
        }
    }

    "depth" {
        executes.args("player" to Args.otherPlayer()) { player ->
            deeperWorld.sections.getDepth(player.location)?.let {
                if (sender is Player) {
                    sender.success("Your depth is $it blocks")
                } else {
                    sender.success("Depth of player ${player.name} is $it blocks")
                }

            } ?: sender.error("${player.name} is not in a managed section")
        }
    }

    "section" {
        "switch" {
            description = "Switches to corresponding section if player is in bondary"
            executes.asPlayer {
                player.teleport(player.location.correspondingLocation ?: fail("No corresponding section"))
            }
        }
        "tp" {
            executes.asPlayer().args(
                "section" to Args.string().oneOf { get<SectionRepository>().sections.map { it.key } }
            ) { section ->
                val center = get<SectionRepository>()[section]?.section?.center ?: fail("Section not found")
                player.teleport(center)
            }
        }
    }
}

