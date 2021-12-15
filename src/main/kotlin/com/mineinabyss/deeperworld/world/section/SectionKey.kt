package com.mineinabyss.deeperworld.world.section

interface SectionKey {
    companion object {
        val TERMINAL: SectionKey = object : SectionKey {
            override fun toString(): String {
                return "TERMINAL"
            }
        }
    }
}
