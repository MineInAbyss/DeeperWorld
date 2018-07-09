package com.derongan.minecraft.deeperworld.event;

import com.derongan.minecraft.deeperworld.world.section.Section;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerAscendEvent extends PlayerChangeSectionEvent {
    public PlayerAscendEvent(Player player, Section fromSection, Section toSection) {
        super(player, fromSection, toSection);
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
