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
import org.bukkit.inventory.meta.BundleMeta;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final ExtraMaceLimiter plugin;
    
    // for player attempts to transfer maces
    private final Map<UUID, Integer> playerAttempts = new HashMap<>();
    
    private final Map<UUID, Integer> playerPickupAttempts = new HashMap<>();

    public InventoryListener(ExtraMaceLimiter plugin) {
        this.plugin = plugin;
    }


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
        if (plugin.getConfig().getBoolean("blocked-storages.furnace", true)) {
            blocked.add(InventoryType.FURNACE);
        }
        if (plugin.getConfig().getBoolean("blocked-storages.blast-furnace", true)) {
            blocked.add(InventoryType.BLAST_FURNACE);
        }
        if (plugin.getConfig().getBoolean("blocked-storages.smoker", true)) {
            blocked.add(InventoryType.SMOKER);
        }
        if (plugin.getConfig().getBoolean("blocked-storages.crafter", true)) {
            blocked.add(InventoryType.CRAFTER);
        }
        
        return blocked;
    }


    private boolean containsMaces(ItemStack item) {
        if (item == null) return false;
        
        // check if item is mace
        if (item.getType() == Material.MACE) {
            return true;
        }
        
        // check if in bundle
        if (plugin.getConfig().getBoolean("block-bundles-with-maces", true) && 
            item.getType() == Material.BUNDLE && item.hasItemMeta()) {
            
            BundleMeta bundleMeta = (BundleMeta) item.getItemMeta();
            if (bundleMeta.hasItems()) {
                for (ItemStack bundledItem : bundleMeta.getItems()) {
                    if (bundledItem != null && bundledItem.getType() == Material.MACE) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        Set<InventoryType> blockedInventories = getBlockedInventories();

        // check the inventory type
        if (blockedInventories.contains(inv.getType())) {
            boolean shouldBlock = false;
            
            // handle clicking the storage inv
            if (event.getRawSlot() < inv.getSize()) {
                ItemStack holding = event.getCursor();

                // check if its a mace or bundle with maces
                if (containsMaces(holding)) {
                    shouldBlock = true;
                }
                
                // the hotkey swapping
                if (event.getHotbarButton() != -1) {
                    ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
                    if (containsMaces(hotbarItem)) {
                        shouldBlock = true;
                    }
                }
            }
            // shift clicking
            else if (event.isShiftClick()) {
                ItemStack clicked = event.getCurrentItem();
                
                if (containsMaces(clicked)) {
                    shouldBlock = true;
                }
            }
            
            // cancelling event
            if (shouldBlock) {
                event.setCancelled(true);
                handleMaceBlockMessage(player);
            }
        }
    }
    

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!plugin.getConfig().getBoolean("block-hopper-pickup", true) && 
            !plugin.getConfig().getBoolean("block-hopper-minecart-pickup", true)) {
            return;
        }
        
        // check if pickup item is a mace
        if (event.getItem().getItemStack().getType() == Material.MACE) {
            Inventory inventory = event.getInventory();
            
            // hopper cancelling
            if (inventory.getType() == InventoryType.HOPPER && 
                plugin.getConfig().getBoolean("block-hopper-pickup", true)) {
                event.setCancelled(true);
                return;
            }
            
            // hopper minecart cancelling
            if (inventory.getHolder() instanceof HopperMinecart && 
                plugin.getConfig().getBoolean("block-hopper-minecart-pickup", true)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {

        if (event.getItem().getType() == Material.MACE) {
            Inventory destination = event.getDestination();
            Set<InventoryType> blockedInventories = getBlockedInventories();
            
            // check destination type
            if (blockedInventories.contains(destination.getType())) {
                event.setCancelled(true);
                return;
            }
            
            // added check for hoppers
            if (destination.getHolder() instanceof HopperMinecart && 
                plugin.getConfig().getBoolean("block-hopper-minecart-pickup", true)) {
                event.setCancelled(true);
                return;
            }
        }
    }


    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!plugin.getConfig().getBoolean("stop-pickup-at-max-maces", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        if (event.getItem().getItemStack().getType() == Material.MACE) {
            int maxMaces = plugin.getConfig().getInt("max-maces-in-inventory", 2);
            
            if (maxMaces == -1) {
                return;
            }
            
            int maceCount = countMacesInInventory(player);
            
            if (maceCount >= maxMaces) {
                event.setCancelled(true);
                handleMacePickupBlockMessage(player);
            }
        }
    }
    

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!plugin.getConfig().getBoolean("block-item-frame-placement", true)) {
            return;
        }
        
        // check if its an item frame
        if (event.getRightClicked() instanceof ItemFrame) {
            Player player = event.getPlayer();
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            
            if (containsMaces(heldItem)) {
                event.setCancelled(true);
                handleMaceBlockMessage(player);
            }
        }
    }


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
    

    private void handleMacePickupBlockMessage(Player player) {
        UUID playerId = player.getUniqueId();
        int attempts = playerPickupAttempts.getOrDefault(playerId, 0) + 1;
        playerPickupAttempts.put(playerId, attempts);
        
        int frequency = plugin.getConfig().getInt("messages.pickup-blocking.frequency", 100);
        
        if (attempts == 1 || attempts % frequency == 0) {
            String message = plugin.getConfig().getString("messages.pickup-blocking.text", 
                "&c&lHey! &7You can't carry more than 2 maces at once.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    

    private void handleMaceBlockMessage(Player player) {
        UUID playerId = player.getUniqueId();
        int attempts = playerAttempts.getOrDefault(playerId, 0) + 1;
        playerAttempts.put(playerId, attempts);
        
        int frequency = plugin.getConfig().getInt("messages.storage-blocking.frequency", 5);
        
        if (attempts == 1 || attempts % frequency == 0) {
            String message = plugin.getConfig().getString("messages.storage-blocking.text", 
                "&c&lHey! &7You can't move a mace outside your inventory.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}