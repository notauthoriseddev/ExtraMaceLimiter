package com.example.nomacesinventories;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InventoryListener implements Listener {

    // Set of inventory types where maces should be blocked
    private static final Set<InventoryType> BLOCKED_INVENTORIES = EnumSet.of(
        InventoryType.CHEST,           // Regular chests
        InventoryType.ENDER_CHEST,     // Ender chests
        InventoryType.BARREL,          // Barrels
        InventoryType.SHULKER_BOX,     // Shulker boxes
        InventoryType.HOPPER,          // Hoppers
        InventoryType.DROPPER,         // Droppers
        InventoryType.DISPENSER        // Dispensers
    );

    // Track player attempts to place maces in blocked inventories
    private final Map<UUID, Integer> playerAttempts = new HashMap<>();
    
    // Track player attempts to pick up maces when at limit (2+ maces)
    private final Map<UUID, Integer> playerPickupAttempts = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Make sure the event involves a player
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();

        // Check if the inventory is one of the blocked storage types
        if (BLOCKED_INVENTORIES.contains(inv.getType())) {
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
        // Check if the item being picked up is a mace
        if (event.getItem().getItemStack().getType() == Material.MACE) {
            Inventory inventory = event.getInventory();
            
            // Check if the inventory trying to pick up is a blocked type
            if (BLOCKED_INVENTORIES.contains(inventory.getType())) {
                event.setCancelled(true);
                return;
            }
            
            // Additional check for hopper minecarts
            if (inventory.getHolder() instanceof HopperMinecart) {
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
            
            // Check if the destination is a blocked inventory type
            if (BLOCKED_INVENTORIES.contains(destination.getType())) {
                event.setCancelled(true);
                return;
            }
            
            // Additional check for hopper minecarts (they might not show up as HOPPER type)
            if (destination.getHolder() instanceof HopperMinecart) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Prevents players from picking up maces if they already have 2 or more in their inventory
     */
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        
        // Check if the item being picked up is a mace
        if (event.getItem().getItemStack().getType() == Material.MACE) {
            // Count how many maces the player already has
            int maceCount = countMacesInInventory(player);
            
            // If player already has 2 or more maces, prevent pickup
            if (maceCount >= 2) {
                event.setCancelled(true);
                handleMacePickupBlockMessage(player);
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
     * Shows message on attempt 1, then every 100 attempts after that (1, 100, 200, 300, etc.)
     */
    private void handleMacePickupBlockMessage(Player player) {
        UUID playerId = player.getUniqueId();
        int attempts = playerPickupAttempts.getOrDefault(playerId, 0) + 1;
        playerPickupAttempts.put(playerId, attempts);
        
        // Show message on first attempt or every 100 attempts after that
        if (attempts == 1 || attempts % 100 == 0) {
            String message = ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Hey! " + 
                           ChatColor.GRAY.toString() + "You can't carry more than 2 maces at once.";
            player.sendMessage(message);
        }
    }
    
    /**
     * Handles sending warning messages to players when they try to place maces in blocked inventories
     * Shows message on first attempt and then every 5 attempts after that
     */
    private void handleMaceBlockMessage(Player player) {
        UUID playerId = player.getUniqueId();
        int attempts = playerAttempts.getOrDefault(playerId, 0) + 1;
        playerAttempts.put(playerId, attempts);
        
        // Show message on first attempt or every 5 attempts after that
        if (attempts == 1 || attempts % 5 == 1) {
            String message = ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Hey! " + 
                           ChatColor.GRAY.toString() + "You can't move a mace outside your inventory.";
            player.sendMessage(message);
        }
    }
}