package com.derongan.minecraft.deeperworld;

import com.derongan.minecraft.deeperworld.player.PlayerManager;
import com.derongan.minecraft.deeperworld.world.WorldManager;
import com.derongan.minecraft.deeperworld.world.WorldManagerImpl;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DeeperWorld extends JavaPlugin {

    private WorldManagerImpl worldManager;
    private MovementListener movementListener;

    @Override
    public void onEnable() {
        createConfig();

        PlayerManager playerManager = new PlayerManager();

        worldManager = new WorldManagerImpl(getConfig());
        this.getServer().getServicesManager().register(WorldManager.class, worldManager, this, ServicePriority.Lowest);

        movementListener = new MovementListener(playerManager);
        this.getServer().getPluginManager().registerEvents(movementListener, this);

        DeeperCommandExecutor commandExecutor = new DeeperCommandExecutor(playerManager);

        this.getCommand("sectionon").setExecutor(commandExecutor);
        this.getCommand("sectionoff").setExecutor(commandExecutor);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                if(!getDataFolder().mkdirs()){
                    throw new RuntimeException("Failed to make config file");
                }
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
