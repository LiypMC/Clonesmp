package com.Liyp.clonesmp;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles all events related to the Mega Head mechanic
 */
public class MegaHeadListener implements Listener {
    private final CloneSMP plugin = CloneSMP.getInstance();

    /**
     * Preview crafting result when ingredients are placed
     * 
     * @param event The PrepareItemCraftEvent
     */
    @EventHandler
    public void onPrepareCraftMegaHead(PrepareItemCraftEvent event) {
        if (!(event.getInventory() instanceof CraftingInventory)) return;
        
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();
        
        // Check for the recipe pattern (3 heads in top row)
        if (!isValidMegaHeadPattern(matrix)) {
            return;
        }
        
        // Get player info from heads
        SkullMeta sm0 = (SkullMeta) matrix[0].getItemMeta();
        if (sm0 == null || sm0.getOwningPlayer() == null) return;
        
        OfflinePlayer owner = sm0.getOwningPlayer();
        // Only allow crafting Mega Heads for banned players
        if (!isBanned(owner)) {
            inv.setResult(null);
            return;
        }
        
        // Create the Mega Head
        ItemStack megaHead = createMegaHead(owner);
        inv.setResult(megaHead);
    }

    /**
     * Handle the actual crafting of Mega Head
     * 
     * @param event The CraftItemEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCraftMegaHead(CraftItemEvent event) {
        if (!(event.getInventory() instanceof CraftingInventory)) return;
        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() != Material.PLAYER_HEAD) return;
        
        // Check if this is a Mega Head
        ItemMeta meta = result.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(plugin.getMegaHeadKey(), PersistentDataType.STRING)) {
            return;
        }
        
        // Play sound effect for crafting
        if (event.getWhoClicked() instanceof Player) {
            Player crafter = (Player) event.getWhoClicked();
            crafter.playSound(crafter.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
            
            // Get player name from UUID
            String uuidStr = meta.getPersistentDataContainer()
                                .get(plugin.getMegaHeadKey(), PersistentDataType.STRING);
            if (uuidStr != null) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
                crafter.sendMessage(ChatColor.GREEN + "You crafted a Mega Head for " + 
                                   ChatColor.GOLD + target.getName() + 
                                   ChatColor.GREEN + "!");
            }
        }
    }

    /**
     * Store data when placing a Mega Head block
     * 
     * @param event The BlockPlaceEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlaceMegaHead(BlockPlaceEvent event) {
        ItemStack hand = event.getItemInHand();
        if (hand.getType() != Material.PLAYER_HEAD) return;
        
        ItemMeta im = hand.getItemMeta();
        if (im == null || !im.getPersistentDataContainer().has(plugin.getMegaHeadKey(), PersistentDataType.STRING)) {
            return;
        }

        // Transfer the data to the placed block
        BlockState bs = event.getBlockPlaced().getState();
        if (!(bs instanceof Skull)) return;
        
        Skull skull = (Skull) bs;
        String uuid = im.getPersistentDataContainer()
                        .get(plugin.getMegaHeadKey(), PersistentDataType.STRING);
                        
        if (uuid != null) {
            skull.getPersistentDataContainer()
                 .set(plugin.getMegaHeadKey(), PersistentDataType.STRING, uuid);
            skull.update();
            
            // Play effect
            Player player = event.getPlayer();
            player.playSound(event.getBlock().getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 1.0f);
        }
    }

    /**
     * Handle breaking a Mega Head to unban a player
     * 
     * @param event The BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakMegaHead(BlockBreakEvent event) {
        Block b = event.getBlock();
        if (b.getType() != Material.PLAYER_HEAD && b.getType() != Material.PLAYER_WALL_HEAD) return;
        
        BlockState bs = b.getState();
        if (!(bs instanceof Skull)) return;
        
        Skull skull = (Skull) bs;
        if (!skull.getPersistentDataContainer().has(plugin.getMegaHeadKey(), PersistentDataType.STRING)) {
            return;
        }

        // Get target player info
        String uuidStr = skull.getPersistentDataContainer()
                             .get(plugin.getMegaHeadKey(), PersistentDataType.STRING);
        if (uuidStr == null) return;
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
        if (!isBanned(target)) return;

        // Check for required tool
        Player breaker = event.getPlayer();
        ItemStack tool = breaker.getInventory().getItemInMainHand();
        
        if (tool == null
         || !tool.getType().name().endsWith("_PICKAXE")
         || !tool.getItemMeta().hasEnchant(Enchantment.UNBREAKING)
         || tool.getEnchantmentLevel(Enchantment.UNBREAKING) != 3) {
            
            // Inform player of requirements
            breaker.sendMessage(ChatColor.RED + "This Mega Head must be broken with an Unbreaking III pickaxe!");
            event.setCancelled(true);
            return;
        }

        // Unban the player
        pardonPlayer(target);
        
        // Reset death count
        plugin.getData().set("deaths." + uuidStr, 0);
        plugin.saveData();

        // Effects and feedback
        breaker.sendMessage(ChatColor.GREEN + "You have unbanned " + ChatColor.GOLD + target.getName() + ChatColor.GREEN + "!");
        Bukkit.broadcastMessage(ChatColor.GOLD + target.getName() + ChatColor.GREEN + " has been unbanned by " + 
                               ChatColor.GOLD + breaker.getName() + ChatColor.GREEN + "!");
        
        // Play effects
        breaker.playSound(b.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.5f, 1.0f);
        
        // Prevent normal drops
        event.setDropItems(false);
    }
    
    /**
     * Create a Mega Head item for the specified player
     * 
     * @param player The player to create a Mega Head for
     * @return The Mega Head ItemStack
     */
    private ItemStack createMegaHead(OfflinePlayer player) {
        ItemStack mega = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) mega.getItemMeta();
        
        if (sm != null) {
            sm.setOwningPlayer(player);
            sm.setDisplayName(ChatColor.RED + "Mega Head of " + player.getName());
            
            List<String> lore = Arrays.asList(
                ChatColor.GOLD + "Crafted by teammates",
                ChatColor.GRAY + "Break with Unbreaking III pickaxe to unban"
            );
            
            sm.setLore(lore);
            sm.getPersistentDataContainer()
              .set(plugin.getMegaHeadKey(), PersistentDataType.STRING, player.getUniqueId().toString());
            mega.setItemMeta(sm);
        }
        
        return mega;
    }
    
    /**
     * Check if the crafting pattern is valid for a Mega Head
     * 
     * @param matrix The crafting matrix
     * @return True if the pattern is valid
     */
    private boolean isValidMegaHeadPattern(ItemStack[] matrix) {
        // Check top row slots 0,1,2
        for (int i = 0; i < 3; i++) {
            if (matrix[i] == null || matrix[i].getType() != Material.PLAYER_HEAD) {
                return false;
            }
        }
        
        // Check if all heads are the same player
        SkullMeta sm0 = (SkullMeta) matrix[0].getItemMeta();
        SkullMeta sm1 = (SkullMeta) matrix[1].getItemMeta();
        SkullMeta sm2 = (SkullMeta) matrix[2].getItemMeta();
        
        if (sm0 == null || sm1 == null || sm2 == null) return false;
        
        OfflinePlayer p0 = sm0.getOwningPlayer();
        OfflinePlayer p1 = sm1.getOwningPlayer();
        OfflinePlayer p2 = sm2.getOwningPlayer();
        
        if (p0 == null || p1 == null || p2 == null) return false;
        
        UUID u = p0.getUniqueId();
        return u.equals(p1.getUniqueId()) && u.equals(p2.getUniqueId());
    }
    
    /**
     * Check if a player is banned
     * 
     * @param player The player to check
     * @return True if the player is banned
     */
    @SuppressWarnings("deprecation")
    private boolean isBanned(OfflinePlayer player) {
        if (player == null || player.getName() == null) return false;
        
        // Simply check if the player name is banned
        return plugin.getServer().getBanList(BanList.Type.NAME).isBanned(player.getName());
    }
    
    /**
     * Pardon (unban) a player
     * 
     * @param player The player to unban
     */
    @SuppressWarnings("deprecation")
    private void pardonPlayer(OfflinePlayer player) {
        if (player == null || player.getName() == null) return;
        
        // Simply pardon the player name
        plugin.getServer().getBanList(BanList.Type.NAME).pardon(player.getName());
    }
}