package com.mineinabyss.deeperworld.nms.coordinate

import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.idofront.nms.interceptClientbound
import com.mineinabyss.idofront.nms.interceptServerbound
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

object CoordinateFixer {

    fun handleIntercept() {
        deeperWorld.plugin.logger.info { "Registering packet interceptors" }
        deeperWorld.plugin.interceptServerbound { packet: Packet<*>, player: Player? ->
            when (packet) {
                is Pos -> return@interceptServerbound handleServerPosPacket(packet, player)
                is PosRot -> return@interceptServerbound handleServerPosRotPacket(packet, player)
                else -> {
                    deeperWorld.plugin.logger.info { "SPacket P: ${packet::class.simpleName}" }

                    return@interceptServerbound packet
                }
            }
        }
        deeperWorld.plugin.interceptClientbound { packet: Packet<*>, player: Player? ->
            when (packet) {
                is ClientboundSetBorderCenterPacket -> return@interceptClientbound null // Ignore this packet
                is ClientboundSetChunkCacheCenterPacket -> return@interceptClientbound handleSetChunkCacheCenterPacket(
                    packet,
                    player!!
                )

                is ClientboundLevelChunkWithLightPacket -> return@interceptClientbound handleLevelChunkWithLightPacket(
                    packet,
                    player!!
                )

                is ClientboundPlayerPositionPacket -> return@interceptClientbound handlePlayerPositionPacket(
                    packet,
                    player
                )

                is ClientboundForgetLevelChunkPacket -> return@interceptClientbound null // Ignore this packet
                else -> {
                    deeperWorld.plugin.logger.info { "CPacket P: ${packet::class.simpleName}" }

                    return@interceptClientbound packet
                }
            }
        }
    }

    private fun handleSetChunkCacheCenterPacket(
        packet: ClientboundSetChunkCacheCenterPacket, player: Player
    ): ClientboundSetChunkCacheCenterPacket {
        deeperWorld.plugin.logger.info {
            "Packet: ${packet::class.simpleName} - ${packet.x} ${packet.z}"
        }

        // Modify packet

        val xField = packet::class.java.getDeclaredField("x")
        val zField = packet::class.java.getDeclaredField("z")
        xField.isAccessible = true
        zField.isAccessible = true

        // get y based off player location
        val deeperLocation = convertToDeeperWorldLocation(
            Location(
                player.world,
                packet.x.toDouble() * 16,
                player.y,
                packet.z.toDouble() * 16
            )
        ) ?: return packet

        // Set the fields to new values
        xField.setInt(packet, deeperLocation.blockX / 16)
        zField.setInt(packet, deeperLocation.blockZ / 16)

        deeperWorld.plugin.logger.info {
            "x: ${deeperLocation.blockX / 16} z: ${deeperLocation.blockZ / 16}"
        }

        return packet
    }

    private fun handleServerPosRotPacket(packet: PosRot, player: Player?): PosRot {
//        deeperWorld.plugin.logger.info {
//            "Original Packet: ${packet::class.simpleName} - x: ${packet.x}, y: ${packet.y}, z: ${packet.z}"
//        }

        // Convert the coordinates to real world coordinates based on the player’s location
        val realLocation = convertToRealWorldLocation(
            Location(player?.world ?: Bukkit.getWorlds().first(), packet.x, packet.y, packet.z)
        ) ?: return packet

//        deeperWorld.plugin.logger.info { "Converted Real Location: $realLocation" }

        // Create a new packet with the modified coordinates
        return PosRot(
            realLocation.x, realLocation.y, realLocation.z, packet.yRot, packet.xRot, packet.isOnGround
        )
    }

    private fun handleServerPosPacket(packet: Pos, player: Player?): Pos {
//        deeperWorld.plugin.logger.info {
//            "Original Packet: ${packet::class.simpleName} - x: ${packet.x}, y: ${packet.y}, z: ${packet.z}"
//        }

        // Convert the coordinates to real world coordinates based on the player’s location
        val realLocation = convertToRealWorldLocation(
            Location(player?.world ?: Bukkit.getWorlds().first(), packet.x, packet.y, packet.z)
        ) ?: return packet

//        deeperWorld.plugin.logger.info { "Converted Real Location: $realLocation" }

        // Create a new packet with the modified coordinates
        return Pos(realLocation.x, realLocation.y, realLocation.z, packet.isOnGround)
    }


    private fun handlePlayerPositionPacket(
        packet: ClientboundPlayerPositionPacket, player: Player?
    ): ClientboundPlayerPositionPacket {
//        deeperWorld.plugin.logger.info {
//            "Packet: ${packet::class.simpleName} - ${packet.x} ${packet.y} ${packet.z}"
//        }

        // Modify packet

        val xField = packet::class.java.getDeclaredField("x")
        val yField = packet::class.java.getDeclaredField("y")
        val zField = packet::class.java.getDeclaredField("z")
        xField.isAccessible = true
        yField.isAccessible = true
        zField.isAccessible = true

        // get y based off player location
        val deeperLocation = convertToDeeperWorldLocation(
            Location(
                player?.world ?: Bukkit.getWorlds().first(),
                packet.x,
                packet.y,
                packet.z
            )
        ) ?: return packet

        // Set the fields to new values
        xField.setDouble(packet, deeperLocation.x)
        yField.setDouble(packet, deeperLocation.y)
        zField.setDouble(packet, deeperLocation.z)

//        deeperWorld.plugin.logger.info {
//            "deeperLocation: $deeperLocation"
//        }

        return packet
    }

    private fun handleLevelChunkWithLightPacket(
        packet: ClientboundLevelChunkWithLightPacket, player: Player
    ): ClientboundLevelChunkWithLightPacket {
        deeperWorld.plugin.logger.info {
            "Packet: ${packet::class.simpleName} - ${packet.x} ${packet.z} ${packet.extraPackets}"
        }

        // Modify packet

        val xField = packet::class.java.getDeclaredField("x")
        val zField = packet::class.java.getDeclaredField("z")
        xField.isAccessible = true
        zField.isAccessible = true

        // get y based off player location
        val deeperLocation = convertToDeeperWorldLocation(
            Location(
                player.world,
                packet.x.toDouble() * 16,
                player.y,
                packet.z.toDouble() * 16
            )
        ) ?: return packet

        // Set the fields to new values
        xField.setInt(packet, deeperLocation.blockX / 16)
        zField.setInt(packet, deeperLocation.blockZ / 16)

        deeperWorld.plugin.logger.info {
            "x: ${deeperLocation.blockX / 16} z: ${deeperLocation.blockZ / 16}"
        }


//        deeperWorld.plugin.logger.info {
//            "Readable Bytes: ${packet.chunkData.readBuffer.readableBytes()}"
//        }
//
//        val chunkData = packet.chunkData.readBuffer
//
//        // Extract the chunk data from the packet
//        val buffer: ByteArray = chunkData.readByteArray()
//        val chunkX: Int = chunkData.getInt(0)
//        val chunkZ: Int = chunkData.getInt(1)
//        val fullChunk: Boolean = chunkData.getBoolean(0)
//        val bitmask: Int = chunkData.getInt(2)
//
//        deeperWorld.plugin.logger.info("Chunk X: $chunkX, Chunk Z: $chunkZ, Full Chunk: $fullChunk, Bitmask: $bitmask")
//        deeperWorld.plugin.logger.info("Buffer Size: ${buffer.size} bytes")

        deeperWorld.plugin.logger.info("" + player.world.coordinateScale)
//        val SECTION_HEIGHT = Bukkit.getWorlds().first().maxHeight
//        val SECTION_WIDTH = 16

//        // Modify the chunk data as needed
//        for (x in 0..15) {
//            for (y in 0..255) {
//                for (z in 0..15) {
//                    val index: Int = getIndex(x, y, z)
//                    try {
//                        val blockId = buffer[index].toInt() and 0xFF
//
//                        // buffer exists, move it to the new location in the buffer based on the new Y value
//                        val newIndex = getIndex(x, y - 256, z)
//                        buffer[newIndex] = buffer[index]
//                        // remove the old value
//                        buffer[index] = 0
//                    } catch (_: Exception) {
//                    }
//                }
//            }
//        }
//
//        val newChunkDataBuffer = packet.chunkData::class.java.getDeclaredField("buffer")
//        newChunkDataBuffer.isAccessible = true
//        newChunkDataBuffer.set(packet.chunkData, buffer)

        return packet
    }

    fun getIndex(x: Int, y: Int, z: Int): Int {
        return y + (z * 16) + (x * 16 * 16)
    }

    fun convertToDeeperWorldLocation(realLocation: Location): Location? {
//        deeperWorld.plugin.logger.info { "Converting $realLocation to DeeperWorld Location" }
        // Retrieve the first section for reference
        val firstSection = WorldManager.sections.firstOrNull() ?: return null
//        deeperWorld.plugin.logger.info { "First Section: $firstSection" }

        // Find the section the current location belongs to
        val currentSection = WorldManager.getSectionFor(realLocation)
            ?: return realLocation // If the location is not within a section, return the original location
        val currentIndex = WorldManager.sections.indexOf(currentSection)
//        deeperWorld.plugin.logger.info { "Current Section: $currentSection" }


        // If we are at Y level -55, we are at l1s0, the client should think it is at Y level -55, once we find a region with a different ref X value from the first section, we will then start calculating the Y coord. So if the user is in l1s1 at Y level 255, the client should think it is at Y level -257 (as this is 1 block below the refBottom of l1s0)

        if (currentSection.referenceTop.x == firstSection.referenceTop.x) {
            // If the reference x values are the same, we are still in the first section, so we don't need to adjust the y value
            return realLocation
        }

        val worldBottom = realLocation.world.minHeight // -256
//        deeperWorld.plugin.logger.info { "World Bottom: $worldBottom" }

        // l1s1 has a refTop of 16384,224,0 and a refBottom of 16384,-256,0
        // The client should have their X coord adjusted to remove the offset from the refTop of l1s1
        val adjustedX = realLocation.x - currentSection.referenceTop.x
        val adjustedZ = realLocation.z - currentSection.referenceTop.z
//        deeperWorld.plugin.logger.info { "Adjusted X: ${realLocation.x} to $adjustedX" }
//        deeperWorld.plugin.logger.info { "Adjusted Z: ${realLocation.z} to $adjustedZ" }
        // The client should have their Y coord adjusted to simulate going deeper
        // User Y will be at 255, but the client should think they are at -257
        // We will find out how deep the user is by

        val sectionsAbove = WorldManager.sections.filter {
            WorldManager.sections.indexOf(it) in 1..<currentIndex
        }
//        deeperWorld.plugin.logger.info { "Sections Above: $sectionsAbove" }

        val sumOfSectionsAbove = sectionsAbove.sumOf {
            it.region.start.y - it.region.end.y
        }
//        deeperWorld.plugin.logger.info { "Sum of Sections Above: $sumOfSectionsAbove" }

        val adjustedY = worldBottom - sumOfSectionsAbove + realLocation.y
//        deeperWorld.plugin.logger.info { "Adjusted Y: ${realLocation.y} to $adjustedY" }

        return Location(realLocation.world, adjustedX, adjustedY, adjustedZ)
    }

    fun convertToRealWorldLocation(deeperWorldLocation: Location): Location? {
//        deeperWorld.plugin.logger.info { "Converting $deeperWorldLocation to Real World Location" }
        // Do the reverse of the conversion
        val firstSection = WorldManager.sections.firstOrNull() ?: return null
//        deeperWorld.plugin.logger.info { "First Section: $firstSection" }

        val worldBottom = deeperWorldLocation.world.minHeight // -256


        // Deep World Location Y = -1366
        // Real World Location Y = 168

        // Find the section the player is actually in based on the Y-coordinate given to us
        //  We are getting the adjusted Y-coordinate So we need to make a loop and add up each section until we reach the correct one
        var currentSection: Section? = null
        var sectionHeightSum = 0
        for (section in WorldManager.sections.filter { WorldManager.sections.indexOf(it) > 0 }) {
            val sectionHeight = section.region.start.y - section.region.end.y
            if (sectionHeightSum + sectionHeight >= -deeperWorldLocation.y) {
                currentSection = section
                break
            }
            sectionHeightSum += sectionHeight
        }
        if (currentSection == null) {
            // If the location is not within a section, return the original location
            return deeperWorldLocation
        }
        if (currentSection.referenceTop.x == firstSection.referenceTop.x) {
            // If the reference x values are the same, we are still in the first section, so we don't need to adjust the y value
            return deeperWorldLocation
        }
//        deeperWorld.plugin.logger.info { "Current Section: $currentSection" }

        // Calculate the height of all the sections above us - already done
//        deeperWorld.plugin.logger.info { "Section Height Sum: $sectionHeightSum" }

        // Keep X and Z coordinates relative to the first section
        val relativeX = deeperWorldLocation.x + currentSection.referenceTop.x
        val relativeZ = deeperWorldLocation.z + currentSection.referenceTop.z
//        deeperWorld.plugin.logger.info { "Adjusted X: $relativeX" }
//        deeperWorld.plugin.logger.info { "Adjusted Z: $relativeZ" }

        // Reverse of above calculation
        val adjustedY = deeperWorldLocation.y + sectionHeightSum - worldBottom
//        deeperWorld.plugin.logger.info { "Adjusted Y: $adjustedY" }

        return Location(deeperWorldLocation.world, relativeX, adjustedY, relativeZ)
    }
}