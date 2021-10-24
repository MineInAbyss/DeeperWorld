package com.derongan.minecraft.deeperworld.world.section

import java.util.*

/**
 * By wrapping the name we can allow users to register
 * sections with any name without restriction.
 */
abstract class AbstractSectionKey(private val key: String) : SectionKey {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val key1 = other as AbstractSectionKey
        return key == key1.key
    }

    override fun hashCode() = Objects.hash(key)

    override fun toString() = key

    //TODO should this be in here?
    class CustomSectionKey(key: String) : AbstractSectionKey(key)
    class InternalSectionKey : AbstractSectionKey(internalKeyCount++.toString())

    companion object {
        //TODO maybe a generator here will be better for future?
        private var internalKeyCount = 0
    }
}