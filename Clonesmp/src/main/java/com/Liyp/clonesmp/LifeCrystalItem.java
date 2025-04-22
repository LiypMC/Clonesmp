package com.Liyp.clonesmp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles Life Crystal item creation and usage
 */
public class LifeCrystalItem implements Listener {
    private final CloneSMP plugin;
    private final NamespacedKey lifeCrystalKey;
    private final int maxLives;
    
    /**
     * Constructor
     * 
     * @param plugin The CloneSMP plugin instance
     */
    public LifeCrystalItem(CloneSMP plugin) {
        this.plugin = plugin;
        this.lifeCrystalKey = new NamespacedKey(plugin, "life_crystal");
        this.maxLives = plugin.getConfig().getInt("max-life-crystals", 5);
        
        // Register the crafting recipe
        registerRecipe();
        
        // Register this class as a listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fLife Crystal system initialized");
    }
    
    /**
     * Register the Life Crystal crafting recipe
     */
    private void registerRecipe() {
        try {
            ItemStack lifeCrystal = createLifeCrystal();
            
            // Create a shaped recipe for the Life Crystal
            ShapedRecipe recipe = new ShapedRecipe(lifeCrystalKey, lifeCrystal);
            
            // Set the shape (E = End Crystal, H = Player Head)
            // This creates a pattern with a Player Head in the center surrounded by End Crystals
            recipe.shape("EEE", "EHE", "EEE");
            
            // Set the ingredients
            recipe.setIngredient('E', Material.END_CRYSTAL);
            recipe.setIngredient('H', Material.PLAYER_HEAD);
            
            // Register the recipe with the server
            Bukkit.addRecipe(recipe);
            
            Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fRegistered Life Crystal recipe");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register Life Crystal recipe: " + e.getMessage());
            Bukkit.getConsoleSender().sendMessage("§c[CloneSMP] §4ERROR: §fFailed to register Life Crystal recipe: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a Life Crystal item
     * 
     * @return Life Crystal ItemStack
     */
    public ItemStack createLifeCrystal() {
        ItemStack crystal = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = crystal.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Life Crystal");
            
            // Set lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Right-click to gain an extra life");
            lore.add(ChatColor.GRAY + "A rare item that can restore a lost life");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Can only be used when you have 1/3 or 2/3 of your lives");
            lore.add(ChatColor.RED + "Maximum Lives: " + ChatColor.WHITE + maxLives);
            meta.setLore(lore);
            
            // Add glow effect - using UNBREAKING enchantment directly
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            // Add custom tag to identify this item
            meta.getPersistentDataContainer().set(lifeCrystalKey, PersistentDataType.BYTE, (byte) 1);
            
            crystal.setItemMeta(meta);
        }
        
        return crystal;
    }
    
    /**
     * Check if an item is a Life Crystal
     * 
     * @param item The ItemStack to check
     * @return True if the item is a Life Crystal
     */
    public boolean isLifeCrystal(ItemStack item) {
        if (item == null || item.getType() != Material.HEART_OF_THE_SEA) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        return meta.getPersistentDataContainer().has(lifeCrystalKey, PersistentDataType.BYTE);
    }
    
    /**
     * Handle right-click usage of Life Crystal
     * 
     * @param event The PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        // Check if it's a right-click with an item
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if the item is a Life Crystal
        if (item != null && isLifeCrystal(item)) {
            event.setCancelled(true);
            useCrystal(player, item);
        }
    }
    
    /**
     * Use a Life Crystal to gain an extra life
     * 
     * @param player The player using the crystal
     * @param item The Life Crystal item
     */
    private void useCrystal(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        int deaths = plugin.getData().getInt("deaths." + uuid, 0);
        int maxAllowedLives = plugin.getMaxLives();
        int currentLives = maxAllowedLives - deaths;
        
        // Check if player has too many lives to use a crystal
        // Only allow usage when at 1/3 or 2/3 of maximum lives
        if (currentLives == maxAllowedLives) {
            // Full health, can't use
            player.sendMessage(ChatColor.RED + "You already have full lives (" + maxAllowedLives + "/" + maxAllowedLives + ")!");
            return;
        } else if (currentLives < 1) {
            // No lives left (banned), can't use
            player.sendMessage(ChatColor.RED + "You have no lives left! Life Crystals cannot resurrect banned players.");
            return;
        } else if (currentLives > (2 * maxAllowedLives / 3)) {
            // More than 2/3 of max, can't use
            player.sendMessage(ChatColor.RED + "You have too many lives (" + currentLives + "/" + maxAllowedLives + ")!");
            player.sendMessage(ChatColor.YELLOW + "Life Crystals can only be used when you have 1/3 or 2/3 of your maximum lives.");
            return;
        }
        
        // Check if player already has maximum allowed by Life Crystals
        if (currentLives >= maxLives) {
            player.sendMessage(ChatColor.RED + "You already have the maximum number of lives (" + maxLives + ")!");
            return;
        }
        
        // Reduce death count to give an extra life
        deaths = Math.max(0, deaths - 1);
        plugin.getData().set("deaths." + uuid, deaths);
        plugin.saveData();
        
        // Update item count (consume the crystal)
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Calculate new lives count
        int newLives = maxAllowedLives - deaths;
        
        // Send message and play effects
        player.sendMessage(ChatColor.GREEN + "You consumed a Life Crystal and gained an extra life!");
        player.sendMessage(ChatColor.GOLD + "Lives: " + ChatColor.WHITE + newLives + "/" + maxAllowedLives);
        
        // Play sound and particle effects
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.2);
        
        // Broadcast message
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GREEN + " used a Life Crystal and now has " + 
                                ChatColor.GOLD + newLives + ChatColor.GREEN + " lives!");
        
        // Log to console
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §f" + player.getName() + " used a Life Crystal. Lives: " + newLives);
    }
    
    /**
     * Handle crafting events to enforce requirements
     * 
     * @param event The PrepareItemCraftEvent
     */
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) return;
        
        ItemStack result = event.getInventory().getResult();
        if (result == null) return;
        
        // Check if crafting a Life Crystal
        if (isLifeCrystal(result)) {
            // The recipe already requires a player head in the center
            // surrounded by end crystals, so no additional checks are needed here
            
            // However, we can keep this event handler for potential future checks
            // For example, we might want to check if the player has permission
            // to craft a Life Crystal
        }
    }
    
    /**
     * Get the maximum allowed lives with Life Crystals
     * 
     * @return Maximum lives count
     */
    public int getMaxLives() {
        return maxLives;
    }
    
    /**
     * Get the NamespacedKey for Life Crystal items
     * 
     * @return NamespacedKey for Life Crystal
     */
    public NamespacedKey getLifeCrystalKey() {
        return lifeCrystalKey;
    }
}