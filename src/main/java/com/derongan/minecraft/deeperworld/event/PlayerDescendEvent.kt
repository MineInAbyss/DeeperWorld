package com.derongan.minecraft.deeperworld.event;

import com.derongan.minecraft.deeperworld.world.section.Section;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerDescendEvent extends PlayerChangeSectionEvent {
    private static final HandlerList handlers = new HandlerList();

    public PlayerDescendEvent(Player player, Section fromSection, Section toSection) {
        super(player, fromSection, toSection);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
