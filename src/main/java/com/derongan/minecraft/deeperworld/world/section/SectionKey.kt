package com.derongan.minecraft.deeperworld.world.section;

public interface SectionKey {
    SectionKey TERMINAL = new SectionKey() {
        @Override
        public String toString() {
            return "TERMINAL";
        }
    };
}
