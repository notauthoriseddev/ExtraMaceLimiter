package com.example.extramacelimiter;

import org.bukkit.plugin.java.JavaPlugin;

public class ExtraMaceLimiter extends JavaPlugin {

    private static ExtraMaceLimiter instance;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Register the event listener
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        
        // Register the command
        ExtraMaceLimiterCommand commandHandler = new ExtraMaceLimiterCommand(this);
        getCommand("extramacelimiter").setExecutor(commandHandler);
        getCommand("extramacelimiter").setTabCompleter(commandHandler);
        
        // Simple startup message
        getLogger().info("Extra Mace Limiter v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Extra Mace Limiter has been disabled!");
    }
    
    public static ExtraMaceLimiter getInstance() {
        return instance;
    }
}