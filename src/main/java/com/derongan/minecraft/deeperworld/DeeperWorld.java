package com.derongan.minecraft.deeperworld;

import com.derongan.minecraft.deeperworld.player.PlayerManager;
import com.derongan.minecraft.deeperworld.world.WorldManager;
import com.derongan.minecraft.deeperworld.world.WorldManagerImpl;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DeeperWorld extends JavaPlugin {
    private WorldManagerImpl worldManager;
    private MovementListener movementListener;
    private SectionSyncListener sectionSyncListener;

    @Override
    public void onEnable() {
        createConfig();

        PlayerManager playerManager = new PlayerManager();

        worldManager = new WorldManagerImpl(getConfig());
        this.getServer().getServicesManager().register(WorldManager.class, worldManager, this, ServicePriority.Lowest);

        movementListener = new MovementListener(playerManager);
        sectionSyncListener = new SectionSyncListener();
        this.getServer().getPluginManager().registerEvents(movementListener, this);
        this.getServer().getPluginManager().registerEvents(sectionSyncListener, this);

        DeeperCommandExecutor commandExecutor = new DeeperCommandExecutor(playerManager, worldManager);

        this.getCommand("sectionon").setExecutor(commandExecutor);
        this.getCommand("sectionoff").setExecutor(commandExecutor);
        this.getCommand("linfo").setExecutor(commandExecutor);
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
