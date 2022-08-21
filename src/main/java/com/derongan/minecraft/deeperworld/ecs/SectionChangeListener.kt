package com.derongan.minecraft.deeperworld.ecs

import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.entity.Player

object SectionChangeListener : GearyListener() {
    private val TargetScope.ascend by get<DeeperWorldSection>()
    private val TargetScope.player by get<Player>()
}
