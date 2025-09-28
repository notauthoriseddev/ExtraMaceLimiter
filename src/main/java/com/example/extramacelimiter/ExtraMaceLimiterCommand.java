package com.example.extramacelimiter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtraMaceLimiterCommand implements CommandExecutor, TabCompleter {

    private final ExtraMaceLimiter plugin;

    public ExtraMaceLimiterCommand(ExtraMaceLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("extramacelimiter.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // If no arguments, show help
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "version":
                handleVersion(sender);
                break;
            case "author":
                handleAuthor(sender);
                break;
            case "help":
                showHelp(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
                sender.sendMessage(ChatColor.GRAY + "Use " + ChatColor.WHITE + "/" + label + " help" + ChatColor.GRAY + " for available commands.");
                break;
        }

        return true;
    }

    /**
     * Handles the reload subcommand
     */
    private void handleReload(CommandSender sender) {
        try {
            // Reload the configuration
            plugin.reloadConfig();
            
            // Send success message
            sender.sendMessage(ChatColor.GREEN + "Extra Mace Limiter configuration reloaded successfully!");
            
            // Show updated config summary
            int maxMaces = plugin.getConfig().getInt("max-maces-in-inventory", 2);
            String maxMacesDisplay = maxMaces == -1 ? "unlimited" : String.valueOf(maxMaces);
            
            sender.sendMessage(ChatColor.GRAY + "Max maces per player: " + ChatColor.WHITE + maxMacesDisplay);
            sender.sendMessage(ChatColor.GRAY + "Blocked storage types: " + ChatColor.WHITE + getBlockedStorageCount() + "/7");
            sender.sendMessage(ChatColor.GRAY + "Hopper pickup blocking: " + ChatColor.WHITE + 
                (plugin.getConfig().getBoolean("block-hopper-pickup", true) ? "enabled" : "disabled"));
            sender.sendMessage(ChatColor.GRAY + "Item frame blocking: " + ChatColor.WHITE + 
                (plugin.getConfig().getBoolean("block-item-frame-placement", true) ? "enabled" : "disabled"));
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
        }
    }

    /**
     * Handles the version subcommand
     */
    private void handleVersion(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Extra Mace Limiter ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "API Version: " + ChatColor.WHITE + plugin.getDescription().getAPIVersion());
        sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + plugin.getDescription().getDescription());
        sender.sendMessage(ChatColor.GOLD + "========================");
    }

    /**
     * Handles the author subcommand
     */
    private void handleAuthor(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Extra Mace Limiter ===");
        sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + plugin.getDescription().getAuthors().get(0));
        sender.sendMessage(ChatColor.YELLOW + "Website: " + ChatColor.WHITE + plugin.getDescription().getWebsite());
        
        // ASCII art display
        sender.sendMessage(ChatColor.AQUA + " _____     _              ___  ___                 _     _           _ _            ");
        sender.sendMessage(ChatColor.AQUA + "|  ___|   | |             |  \\/  |                | |   (_)         (_) |           ");
        sender.sendMessage(ChatColor.AQUA + "| |____  _| |_ _ __ __ _  | .  . | __ _  ___ ___  | |    _ _ __ ___  _| |_ ___ _ __ ");
        sender.sendMessage(ChatColor.AQUA + "|  __\\ \\/ / __| '__/ _` | | |\\/| |/ _` |/ __/ _ \\ | |   | | '_ ` _ \\| | __/ _ \\ '__|");
        sender.sendMessage(ChatColor.AQUA + "| |___>  <| |_| | | (_| | | |  | | (_| | (_|  __/ | |___| | | | | | | | ||  __/ |   ");
        sender.sendMessage(ChatColor.AQUA + "\\____/_/\\_\\\\__|_|  \\__,_| \\_|  |_/\\__,_|\\___\\___| \\_____/_|_| |_| |_|_|\\__\\___|_|   ");
        sender.sendMessage(ChatColor.GOLD + "========================");
    }

    /**
     * Shows the help menu
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Extra Mace Limiter Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/extramacelimiter reload" + ChatColor.GRAY + " - Reload the plugin configuration");
        sender.sendMessage(ChatColor.YELLOW + "/extramacelimiter version" + ChatColor.GRAY + " - Show plugin version information");
        sender.sendMessage(ChatColor.YELLOW + "/extramacelimiter author" + ChatColor.GRAY + " - Show plugin author and ASCII art");
        sender.sendMessage(ChatColor.YELLOW + "/extramacelimiter help" + ChatColor.GRAY + " - Show this help menu");
        sender.sendMessage(ChatColor.GOLD + "Aliases: " + ChatColor.WHITE + "/eml, /macelimiter");
        sender.sendMessage(ChatColor.GOLD + "==================================");
    }

    /**
     * Counts how many storage types are blocked in the config
     */
    private int getBlockedStorageCount() {
        int count = 0;
        String[] storageTypes = {"chest", "ender-chest", "barrel", "shulker-box", "hopper", "dropper", "dispenser"};
        
        for (String storage : storageTypes) {
            if (plugin.getConfig().getBoolean("blocked-storages." + storage, true)) {
                count++;
            }
        }
        
        return count;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Check permission for tab completion
        if (!sender.hasPermission("extramacelimiter.admin")) {
            return completions;
        }

        if (args.length == 1) {
            // First argument - subcommands
            String[] subCommands = {"reload", "version", "author", "help"};
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        }

        return completions;
    }
}