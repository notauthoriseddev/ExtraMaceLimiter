package com.example.extramacelimiter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final ExtraMaceLimiter plugin;
    
    // Track player attempts to place maces in blocked inventories
    private final Map<UUID, Integer> playerAttempts = new HashMap<>();
    
    // Track player attempts to pick up maces when at limit (2+ maces)
    private final Map<UUID, Integer> playerPickupAttempts = new HashMap<>();

    public InventoryListener(ExtraMaceLimiter plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the set of blocked inventory types based on config settings
     */
    private Set<InventoryType> getBlockedInventories() {
        Set<InventoryType> blocked = EnumSet.noneOf(InventoryType.class);
        
        if (plugin.getConfig().getBoolean("blocked-storages.chest", true)) {
            blocked.add(InventoryType.CHEST);
        }
        if (plugin.getConfig().getBoolean("blocked-storages.ender-chest", true)) {
            blocked.add(InventoryType.ENDER_CHEST);
        }
        if (plugin.getConfig().getBoolean("blocked-storages.barrel", true)) {
            blocked.add(InventoryType.BARREL);
        }
        if (plugin.getConfig().getBoolean("blocked-storages.shulker-box", true)) {
            blocked.add(InventoryType.SHULKER_BOX);
        }
        if (plugin.getConfig().getBoolean("blocked-storages.hopper", true)) {
            blocked.add(InventoryType.HOPPER);
        }
        if (plugin.getConfig().getBoolean("blocked-storages.dropper", true)) {
            blocked.add(InventoryType.DROPPER);
        }
        if (plugin.getConfig().getBoolean("blocked-storages.dispenser", true)) {
            blocked.add(InventoryType.DISPENSER);
        }
        
        return blocked;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Make sure the event involves a player
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        Set<InventoryType> blockedInventories = getBlockedInventories();

        // Check if the inventory is one of the blocked storage types
        if (blockedInventories.contains(inv.getType())) {
            boolean shouldBlock = false;
            
            // Handle clicking in the storage inventory itself (not player inventory)
            if (event.getRawSlot() < inv.getSize()) {
                ItemStack holding = event.getCursor();

                // Check if the player is trying to place a mace
                if (holding != null && holding.getType() == Material.MACE) {
                    shouldBlock = true;
                }
                
                // Handle hotkey swaps (pressing number keys 1-9)
                if (event.getHotbarButton() != -1) {
                    ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
                    if (hotbarItem != null && hotbarItem.getType() == Material.MACE) {
                        shouldBlock = true;
                    }
                }
            }
            // Handle shift-clicking from player inventory to storage inventory
            else if (event.isShiftClick()) {
                ItemStack clicked = event.getCurrentItem();
                
                // Check if the clicked item is a mace
                if (clicked != null && clicked.getType() == Material.MACE) {
                    shouldBlock = true;
                }
            }
            
            // If we should block the action, cancel it and handle the message
            if (shouldBlock) {
                event.setCancelled(true);
                handleMaceBlockMessage(player);
            }
        }
    }
    
    /**
     * Prevents hoppers from picking up maces from the ground
     */
    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        // Check config settings for hopper pickup blocking
        if (!plugin.getConfig().getBoolean("block-hopper-pickup", true) && 
            !plugin.getConfig().getBoolean("block-hopper-minecart-pickup", true)) {
            return;
        }
        
        // Check if the item being picked up is a mace
        if (event.getItem().getItemStack().getType() == Material.MACE) {
            Inventory inventory = event.getInventory();
            
            // Check if it's a hopper and hopper pickup is blocked
            if (inventory.getType() == InventoryType.HOPPER && 
                plugin.getConfig().getBoolean("block-hopper-pickup", true)) {
                event.setCancelled(true);
                return;
            }
            
            // Check if it's a hopper minecart and hopper minecart pickup is blocked
            if (inventory.getHolder() instanceof HopperMinecart && 
                plugin.getConfig().getBoolean("block-hopper-minecart-pickup", true)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    /**
     * Prevents hoppers and hopper minecarts from moving maces between inventories
     */
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        // Check if the item being moved is a mace
        if (event.getItem().getType() == Material.MACE) {
            Inventory destination = event.getDestination();
            Set<InventoryType> blockedInventories = getBlockedInventories();
            
            // Check if the destination is a blocked inventory type
            if (blockedInventories.contains(destination.getType())) {
                event.setCancelled(true);
                return;
            }
            
            // Additional check for hopper minecarts (they might not show up as HOPPER type)
            if (destination.getHolder() instanceof HopperMinecart && 
                plugin.getConfig().getBoolean("block-hopper-minecart-pickup", true)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Prevents players from picking up maces if they already have the maximum amount
     */
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        // Check if pickup blocking is enabled
        if (!plugin.getConfig().getBoolean("stop-pickup-at-max-maces", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Check if the item being picked up is a mace
        if (event.getItem().getItemStack().getType() == Material.MACE) {
            // Get max maces from config
            int maxMaces = plugin.getConfig().getInt("max-maces-in-inventory", 2);
            
            // If unlimited maces (-1), don't block
            if (maxMaces == -1) {
                return;
            }
            
            // Count how many maces the player already has
            int maceCount = countMacesInInventory(player);
            
            // If player already has max or more maces, prevent pickup
            if (maceCount >= maxMaces) {
                event.setCancelled(true);
                handleMacePickupBlockMessage(player);
            }
        }
    }
    
    /**
     * Prevents players from placing maces in item frames
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // Check if item frame blocking is enabled
        if (!plugin.getConfig().getBoolean("block-item-frame-placement", true)) {
            return;
        }
        
        // Check if the entity is an item frame (includes glow item frames)
        if (event.getRightClicked() instanceof ItemFrame) {
            Player player = event.getPlayer();
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            
            // Check if the player is trying to place a mace in the item frame
            if (heldItem != null && heldItem.getType() == Material.MACE) {
                event.setCancelled(true);
                handleMaceBlockMessage(player);
            }
        }
    }

    /**
     * Counts the number of maces in a player's inventory (including hotbar)
     */
    private int countMacesInInventory(Player player) {
        int count = 0;
        ItemStack[] contents = player.getInventory().getContents();
        
        for (ItemStack item : contents) {
            if (item != null && item.getType() == Material.MACE) {
                count += item.getAmount();
            }
        }
        
        return count;
    }
    
    /**
     * Handles sending warning messages to players when they try to pick up maces over the limit
     * Shows message based on config frequency settings
     */
    private void handleMacePickupBlockMessage(Player player) {
        UUID playerId = player.getUniqueId();
        int attempts = playerPickupAttempts.getOrDefault(playerId, 0) + 1;
        playerPickupAttempts.put(playerId, attempts);
        
        // Get frequency from config (default: 100)
        int frequency = plugin.getConfig().getInt("messages.pickup-blocking.frequency", 100);
        
        // Show message on first attempt or every X attempts after that
        if (attempts == 1 || attempts % frequency == 0) {
            String message = plugin.getConfig().getString("messages.pickup-blocking.text", 
                "&c&lHey! &7You can't carry more than 2 maces at once.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    /**
     * Handles sending warning messages to players when they try to place maces in blocked inventories
     * Shows message based on config frequency settings
     */
    private void handleMaceBlockMessage(Player player) {
        UUID playerId = player.getUniqueId();
        int attempts = playerAttempts.getOrDefault(playerId, 0) + 1;
        playerAttempts.put(playerId, attempts);
        
        // Get frequency from config (default: 5)
        int frequency = plugin.getConfig().getInt("messages.storage-blocking.frequency", 5);
        
        // Show message on first attempt or every X attempts after that
        if (attempts == 1 || attempts % frequency == 0) {
            String message = plugin.getConfig().getString("messages.storage-blocking.text", 
                "&c&lHey! &7You can't move a mace outside your inventory.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}