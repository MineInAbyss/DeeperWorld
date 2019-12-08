package com.derongan.minecraft.deeperworld.event;

import com.derongan.minecraft.deeperworld.world.section.Section;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public abstract class PlayerChangeSectionEvent extends PlayerEvent implements Cancellable {
    private boolean cancelled;

    private Section fromSection;
    private Section toSection;

    public PlayerChangeSectionEvent(Player player, Section fromSection, Section toSection) {
        super(player);

        this.fromSection = fromSection;
        this.toSection = toSection;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Section getFromSection() {
        return fromSection;
    }

    public Section getToSection() {
        return toSection;
    }
}
