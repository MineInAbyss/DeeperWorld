package com.derongan.minecraft.deeperworld;

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent;
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent;
import com.derongan.minecraft.deeperworld.player.PlayerManager;
import com.derongan.minecraft.deeperworld.world.section.Section;
import com.derongan.minecraft.deeperworld.world.section.SectionUtils;
import com.derongan.minecraft.deeperworld.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.function.BiConsumer;

import static com.derongan.minecraft.deeperworld.MinecraftConstants.WORLD_HEIGHT;

public class MovementListener implements Listener {
    private WorldManager worldManager;
    private PlayerManager playerManager;

    public MovementListener(PlayerManager playerManager) {
        worldManager = Bukkit.getServicesManager().load(WorldManager.class);
        this.playerManager = playerManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        Player player = playerMoveEvent.getPlayer();

        if (player.hasPermission(Permissions.CHANGE_SECTION_PERMISSION) && playerManager.playerCanTeleport(player)) {
            onPlayerMoveInternal(player, playerMoveEvent.getFrom(), playerMoveEvent.getTo());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreakEvent(BlockBreakEvent blockBreakEvent){
        Block block = blockBreakEvent.getBlock();

        updateCorrespondingBlock(block.getLocation(), (orig, corr)->{
            corr.setType(Material.AIR);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChangeEvent(SignChangeEvent signChangeEvent){
        Block block = signChangeEvent.getBlock();
        Location location = block.getLocation();

        updateCorrespondingBlock(location, (orig, corr)->{
            UPDATE_BLOCK_DATA.accept(orig, corr);

            if(corr.getState() instanceof Sign){
                Sign sign = (Sign) corr.getState();

                if(!Arrays.equals(sign.getLines(), signChangeEvent.getLines())) {
                    for (int i = 0; i < signChangeEvent.getLines().length; i++) {
                        sign.setLine(i, signChangeEvent.getLine(i));
                    }
                }

                sign.update();
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlaceEvent(BlockPlaceEvent blockPlaceEvent){
        Block block = blockPlaceEvent.getBlock();
        Location location = block.getLocation();

        updateCorrespondingBlock(location, UPDATE_BLOCK_DATA);
    }

    private void updateCorrespondingBlock(Location original, BiConsumer<Block, Block> updater){
        Section section = worldManager.getSectionFor(original);

        if(section != null) {
            Section above = worldManager.getSectionFor(section.getKeyForSectionAbove());
            Section below = worldManager.getSectionFor(section.getKeyForSectionBelow());

            Location corresponding = null;
            if (above != null && SectionUtils.isSharedLocation(section, above, original)) {
                corresponding = SectionUtils.getCorrespondingLocation(section, above, original);
            } else if (below != null && SectionUtils.isSharedLocation(section, below, original)) {
                corresponding = SectionUtils.getCorrespondingLocation(section, below, original);
            }

            if(corresponding != null) {
                updater.accept(original.getBlock(), corresponding.getBlock());
            }
        }
    }

    private static BiConsumer<Block, Block> UPDATE_BLOCK_DATA = (original, corresponding) -> {
        corresponding.setBlockData(original.getBlockData().clone());
    };

    private void onPlayerMoveInternal(Player player, Location from, Location to) {
        double changeY = to.getY() - from.getY();

        if (changeY > 0) {
            Section current = worldManager.getSectionFor(player.getLocation());

            if (current != null) {
                Section above = worldManager.getSectionFor(current.getKeyForSectionAbove());

                if (above != null) {
                    int shared = SectionUtils.getSharedBlocks(current, above);

                    if (to.getY() > WORLD_HEIGHT - .3 * shared) {
                        ascend(player, to, current, above);
                    }
                }
            }
        } else if (changeY < 0) {
            Section current = worldManager.getSectionFor(player.getLocation());
            if (current != null) {

                Section below = worldManager.getSectionFor(current.getKeyForSectionBelow());

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
        PlayerDescendEvent event = new PlayerDescendEvent(player, oldSection, newSection);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            teleportBetweenSections(player, to, oldSection, newSection);
        }
    }

    private void ascend(Player player, Location to, Section oldSection, Section newSection) {
        PlayerAscendEvent event = new PlayerAscendEvent(player, oldSection, newSection);
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
