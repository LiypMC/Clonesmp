package com.Liyp.clonesmp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles player death events and implements the lives system
 */
public class DeathListener implements Listener {
    private final CloneSMP plugin = CloneSMP.getInstance();

    /**
     * Handle player death events
     * 
     * @param event The PlayerDeathEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        // Increment death count & save
        int deaths = plugin.getData().getInt("deaths." + uuid, 0) + 1;
        int maxLives = plugin.getMaxLives();
        int livesLeft = maxLives - deaths;
        
        plugin.getData().set("deaths." + uuid, deaths);
        plugin.saveData();

        // Log and possibly broadcast death
        String deathMessage = ChatColor.RED + player.getName() + 
                              ChatColor.YELLOW + " has " + 
                              ChatColor.RED + livesLeft + 
                              ChatColor.YELLOW + " lives remaining!";
        
        plugin.getLogger().info(player.getName() + " has died " + deaths + " times.");
        
        if (plugin.shouldBroadcastDeaths()) {
            Bukkit.broadcastMessage(deathMessage);
        } else {
            player.sendMessage(deathMessage);
        }

        // Drop a custom player head
        if (plugin.shouldDropHeadOnDeath()) {
            ItemStack head = createPlayerHead(player);
            event.getDrops().add(head);
        }

        // On final death → ban & kick via updated API
        if (deaths >= maxLives) {
            banPlayer(player);
        }
    }
    
    /**
     * Create a custom player head item
     * 
     * @param player The player whose head to create
     * @return ItemStack of player head
     */
    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(ChatColor.GOLD + player.getName() + "'s Head");
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Dropped on death");
            lore.add(ChatColor.GRAY + "Can be used to craft a Mega Head");
            
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }
    
    /**
     * Ban a player who has used all their lives
     * 
     * @param player The player to ban
     */
    @SuppressWarnings("deprecation")
    private void banPlayer(Player player) {
        try {
            // Use available ban method in a compatible way
            plugin.getServer()
                  .getBanList(BanList.Type.NAME)  // Use NAME as it's still supported
                  .addBan(
                      player.getName(),
                      "You died " + plugin.getMaxLives() + " times and have been banned!",
                      null,  // null = permanent
                      plugin.getName()
                  );
                                  
            plugin.getLogger().info("Banned player " + player.getName() + " for using all lives");
            
            // Kick next tick to ensure proper message delivery
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.kickPlayer(
                            ChatColor.RED + "You died " + plugin.getMaxLives() + " times and have been banned.\n" +
                            ChatColor.YELLOW + "Have your team craft a Mega Head to unban you!"
                        );
                    }
                }
            }.runTaskLater(plugin, 1L);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to ban player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}