package com.derongan.minecraft.deeperworld;

import com.derongan.minecraft.deeperworld.event.AscendEvent;
import com.derongan.minecraft.deeperworld.event.DescendEvent;
import com.derongan.minecraft.deeperworld.world.Section;
import com.derongan.minecraft.deeperworld.world.SectionUtils;
import com.derongan.minecraft.deeperworld.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import static com.derongan.minecraft.deeperworld.MinecraftConstants.WORLD_HEIGHT;

public class MovementListener implements Listener {
    private WorldManager manager;

    public MovementListener() {
        manager = Bukkit.getServicesManager().load(WorldManager.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        Player player = playerMoveEvent.getPlayer();

        if (player.hasPermission(Permissions.CHANGE_SECTION_PERMISSION)) {
            onPlayerMoveInternal(player, playerMoveEvent.getFrom(), playerMoveEvent.getTo());
        }
    }

    private void onPlayerMoveInternal(Player player, Location from, Location to) {
        double changeY = to.getY() - from.getY();

        if (changeY > 0) {
            Section current = manager.getSectionFor(player.getLocation());

            if (current != null) {
                Section above = current.getSectionAbove();

                if (above != null) {
                    int shared = SectionUtils.getSharedBlocks(current, above);

                    if (to.getY() > WORLD_HEIGHT - .3 * shared) {
                        ascend(player, to, current, above);
                    }
                }
            }
        } else if (changeY < 0) {
            Section current = manager.getSectionFor(player.getLocation());
            if (current != null) {

                Section below = current.getSectionBelow();

                if (below != null) {
                    int shared = SectionUtils.getSharedBlocks(current, below);

                    if (to.getY() < .3 * shared) {
                        descend(player, to, current, below);
                    }
                }
            }
        }
    }


    private void descend(Player player, Location to, Section oldSection, Section newSection) {
        DescendEvent event = new DescendEvent();
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            teleportBetweenSections(player, to, oldSection, newSection);
        }
    }

    private void ascend(Player player, Location to, Section oldSection, Section newSection) {
        AscendEvent event = new AscendEvent();
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            teleportBetweenSections(player, to, oldSection, newSection);
        }
    }

    private void teleportBetweenSections(Player player, Location to, Section oldSection, Section newSection) {
        Location newLoc = SectionUtils.getCorrespondingLocation(oldSection, newSection, to);

        Vector oldVelocity = player.getVelocity();
        player.teleport(newLoc);
        player.setVelocity(oldVelocity);

    }
}
