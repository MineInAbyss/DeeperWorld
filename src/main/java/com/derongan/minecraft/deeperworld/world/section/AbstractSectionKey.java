package com.derongan.minecraft.deeperworld.world.section;

import com.derongan.minecraft.deeperworld.world.WorldManagerImpl;

import java.util.Objects;

/**
 * By wrapping the name we can allow users to register
 * sections with any name without restriction.
 */
public abstract class AbstractSectionKey implements SectionKey {
    //TODO maybe a generator here will be better for future?
    private static int internalKeyCount;
    private String key;

    AbstractSectionKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractSectionKey key1 = (AbstractSectionKey) o;
        return Objects.equals(key, key1.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    //TODO should this be in here?
    public static class CustomSectionKey extends AbstractSectionKey {
        public CustomSectionKey(String key) {
            super(key);
        }
    }

    public static class InternalSectionKey extends AbstractSectionKey {
        public InternalSectionKey() {
            super(String.valueOf(internalKeyCount++));
        }
    }

    @Override
    public String toString() {
        return key;
    }
}
