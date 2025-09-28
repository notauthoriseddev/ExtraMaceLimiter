package com.example.extramacelimiter;

import org.bukkit.plugin.java.JavaPlugin;

public class ExtraMaceLimiter extends JavaPlugin {

    private static ExtraMaceLimiter instance;

    @Override
    public void onEnable() {
        instance = this;
        
    
        saveDefaultConfig();
        
        // event listener
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        
        // command listener
        ExtraMaceLimiterCommand commandHandler = new ExtraMaceLimiterCommand(this);
        getCommand("extramacelimiter").setExecutor(commandHandler);
        getCommand("extramacelimiter").setTabCompleter(commandHandler);
        
        // startup message
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