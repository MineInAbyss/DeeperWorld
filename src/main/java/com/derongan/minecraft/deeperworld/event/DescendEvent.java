package com.derongan.minecraft.deeperworld.event;

import org.bukkit.event.HandlerList;

public class DescendEvent extends SectionChangeEvent {
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
