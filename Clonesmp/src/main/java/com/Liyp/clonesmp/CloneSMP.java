package com.Liyp.clonesmp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * CloneSMP - A survival plugin that implements a lives system with player heads
 * 
 * Features:
 * - Players have limited lives (configurable)
 * - Players drop their head on death
 * - Players are banned after using all lives
 * - Teammates can craft a Mega Head to unban players
 * - Life Crystals can be crafted to gain an extra life
 */
public class CloneSMP extends JavaPlugin {
    private static CloneSMP instance;
    private File dataFile;
    private YamlConfiguration dataConfig;
    private File configFile;
    private NamespacedKey megaHeadKey;
    private LifeCrystalItem lifeCrystalItem;
    
    // Default configuration values
    private int maxLives = 3;
    private boolean broadcastDeaths = true;
    private boolean dropHeadOnDeath = true;
    private boolean lifeCrystalsEnabled = true;

    @Override
    public void onEnable() {
        instance = this;
        
        // Enhanced console logging
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fPlugin is starting up...");
        
        try {
            megaHeadKey = new NamespacedKey(this, "mega_head");
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fCreated MegaHead NamespacedKey");

            // Initialize config files
            setupConfigs();
            
            // Register commands
            getCommand("clonesmp").setExecutor(new CommandHandler(this));
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fRegistered commands");
            
            // Register listeners
            getServer().getPluginManager().registerEvents(new DeathListener(), this);
            getServer().getPluginManager().registerEvents(new MegaHeadListener(), this);
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fRegistered event listeners");
            
            // Initialize Life Crystal feature if enabled
            if (lifeCrystalsEnabled) {
                lifeCrystalItem = new LifeCrystalItem(this);
                Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fLife Crystal feature enabled");
            }

            // Send console messages in multiple formats to ensure visibility
            getLogger().info("====== CloneSMP Enabled ======");
            getLogger().info("Players have " + maxLives + " lives before being banned");
            
            // Direct console messages that should be more visible
            Bukkit.getConsoleSender().sendMessage("§e====== §a[CloneSMP] §2ENABLED §e======");
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fPlayers have §e" + maxLives + " §flives before being banned");
            if (lifeCrystalsEnabled) {
                Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fPlayers can use Life Crystals to get extra lives (max: §e" + 
                                                      getConfig().getInt("max-life-crystals", 5) + "§f)");
            }
            
            // Try severe logging level as well
            getLogger().log(Level.SEVERE, "CloneSMP Plugin Enabled Successfully");
        } catch (Exception e) {
            // Catch any startup errors and log them
            getLogger().log(Level.SEVERE, "Error during plugin startup", e);
            Bukkit.getConsoleSender().sendMessage("§c[CloneSMP] §4ERROR: §fFailed to start plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            saveData();
            getLogger().info("====== CloneSMP Disabled ======");
            Bukkit.getConsoleSender().sendMessage("§e====== §c[CloneSMP] §4DISABLED §e======");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin shutdown", e);
            Bukkit.getConsoleSender().sendMessage("§c[CloneSMP] §4ERROR: §fFailed during shutdown: " + e.getMessage());
        }
    }

    /**
     * Set up configuration and data files
     */
    private void setupConfigs() {
        // Create plugin folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fCreated plugin directory");
        }
        
        // Plugin config setup (config.yml)
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fCreated default config.yml");
        } else {
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fLoaded existing config.yml");
        }
        
        // Load config values directly from getConfig()
        loadConfigValues();
        
        // Data file setup (data.yml)
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fCreated new data.yml file");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not create data.yml", e);
                Bukkit.getConsoleSender().sendMessage("§c[CloneSMP] §4ERROR: §fCould not create data.yml: " + e.getMessage());
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fLoaded existing data.yml");
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    /**
     * Load configuration values from config.yml
     */
    private void loadConfigValues() {
        // Generate default config if not present
        saveDefaultConfig();
        
        // Load values
        maxLives = getConfig().getInt("max-lives", 3);
        broadcastDeaths = getConfig().getBoolean("broadcast-deaths", true);
        dropHeadOnDeath = getConfig().getBoolean("drop-head-on-death", true);
        lifeCrystalsEnabled = getConfig().getBoolean("life-crystal.enabled", true);
        
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fLoaded configuration values:");
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §f- Max Lives: §e" + maxLives);
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §f- Broadcast Deaths: §e" + broadcastDeaths);
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §f- Drop Head on Death: §e" + dropHeadOnDeath);
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §f- Life Crystals Enabled: §e" + lifeCrystalsEnabled);
    }
    
    /**
     * Get the plugin instance
     * @return The CloneSMP instance
     */
    public static CloneSMP getInstance() {
        return instance;
    }

    /**
     * Get the NamespacedKey for Mega Head items
     * @return NamespacedKey for Mega Head
     */
    public NamespacedKey getMegaHeadKey() {
        return megaHeadKey;
    }

    /**
     * Get the plugin data configuration
     * @return YamlConfiguration for data storage
     */
    public YamlConfiguration getData() {
        return dataConfig;
    }

    /**
     * Save the plugin data to file
     */
    public void saveData() {
        try {
            dataConfig.save(dataFile);
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fSaved plugin data to data.yml");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save data.yml", e);
            Bukkit.getConsoleSender().sendMessage("§c[CloneSMP] §4ERROR: §fCould not save data.yml: " + e.getMessage());
        }
    }
    
    /**
     * Get maximum number of lives before ban
     * @return Max lives count
     */
    public int getMaxLives() {
        return maxLives;
    }
    
    /**
     * Check if death messages should be broadcast
     * @return True if deaths should be broadcast
     */
    public boolean shouldBroadcastDeaths() {
        return broadcastDeaths;
    }
    
    /**
     * Check if player heads should drop on death
     * @return True if heads should drop on death
     */
    public boolean shouldDropHeadOnDeath() {
        return dropHeadOnDeath;
    }
    
    /**
     * Get the Life Crystal manager
     * @return The Life Crystal manager instance
     */
    public LifeCrystalItem getLifeCrystalManager() {
        return lifeCrystalItem;
    }
    
    /**
     * Check if Life Crystals are enabled
     * @return True if Life Crystals are enabled
     */
    public boolean areLifeCrystalsEnabled() {
        return lifeCrystalsEnabled;
    }
    
    /**
     * Reload the plugin configuration
     */
    public void reloadConfigs() {
        try {
            reloadConfig();
            loadConfigValues();
            dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            
            // Reinitialize Life Crystal feature if enabled/disabled state changed
            boolean newLifeCrystalsEnabled = getConfig().getBoolean("life-crystal.enabled", true);
            if (newLifeCrystalsEnabled != lifeCrystalsEnabled) {
                lifeCrystalsEnabled = newLifeCrystalsEnabled;
                if (lifeCrystalsEnabled) {
                    lifeCrystalItem = new LifeCrystalItem(this);
                    Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fLife Crystal feature enabled");
                } else {
                    lifeCrystalItem = null;
                    Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fLife Crystal feature disabled");
                }
            }
            
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fPlugin configuration reloaded successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error reloading configuration", e);
            Bukkit.getConsoleSender().sendMessage("§c[CloneSMP] §4ERROR: §fFailed to reload configuration: " + e.getMessage());
        }
    }
}