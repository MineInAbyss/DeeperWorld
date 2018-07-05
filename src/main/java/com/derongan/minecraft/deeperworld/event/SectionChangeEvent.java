package com.derongan.minecraft.deeperworld.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class SectionChangeEvent extends Event implements Cancellable {
    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }
}
