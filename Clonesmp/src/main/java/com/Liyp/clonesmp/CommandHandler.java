package com.Liyp.clonesmp;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * Handles all commands for the CloneSMP plugin
 */
public class CommandHandler implements CommandExecutor, TabCompleter {
    private final CloneSMP plugin;
    
    /**
     * Constructor
     * 
     * @param plugin The CloneSMP plugin instance
     */
    public CommandHandler(CloneSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("clonesmp.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                
                plugin.reloadConfigs();
                sender.sendMessage(ChatColor.GREEN + "CloneSMP configuration reloaded!");
                return true;
                
            case "lives":
                if (args.length < 2) {
                    // Show sender's lives if they're a player
                    if (sender instanceof Player) {
                        showLives((Player) sender, (Player) sender);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /clonesmp lives <player>");
                    }
                } else {
                    // Check permissions for checking others' lives
                    if (args.length > 1 && !sender.equals(Bukkit.getPlayerExact(args[1])) && 
                        !sender.hasPermission("clonesmp.admin.lives")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to check other players' lives.");
                        return true;
                    }
                    
                    // Use Bukkit.getPlayer first to try to find online players
                    Player onlineTarget = Bukkit.getPlayer(args[1]);
                    if (onlineTarget != null) {
                        showPlayerLives(sender, onlineTarget);
                        return true;
                    }
                    
                    // For offline players, try to find by exact UUID if possible
                    try {
                        UUID targetUUID = UUID.fromString(args[1]);
                        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetUUID);
                        if (offlineTarget.hasPlayedBefore()) {
                            showPlayerLives(sender, offlineTarget);
                            return true;
                        }
                    } catch (IllegalArgumentException ignored) {
                        // Not a UUID, continue to check by name
                    }
                    
                    // Last resort: try by name (with warning about potential inaccuracy)
                    OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                    for (OfflinePlayer offlinePlayer : offlinePlayers) {
                        if (offlinePlayer.getName() != null && 
                            offlinePlayer.getName().equalsIgnoreCase(args[1])) {
                            showPlayerLives(sender, offlinePlayer);
                            return true;
                        }
                    }
                    
                    sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                }
                return true;
                
            case "reset":
                if (!sender.hasPermission("clonesmp.admin.reset")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /clonesmp reset <player>");
                    return true;
                }
                
                resetPlayerLives(sender, args[1]);
                return true;
                
            case "debug":
                if (!sender.hasPermission("clonesmp.admin.debug")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                
                sendDebugMessages(sender);
                return true;
                
            case "help":
            default:
                sendHelpMessage(sender);
                return true;
        }
    }
    
    /**
     * Send debug messages to console using various methods
     * 
     * @param sender The command sender
     */
    private void sendDebugMessages(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Sending debug messages to console...");
        
        // Test different logging methods
        plugin.getLogger().info("DEBUG: CloneSMP INFO message test");
        plugin.getLogger().warning("DEBUG: CloneSMP WARNING message test");
        plugin.getLogger().severe("DEBUG: CloneSMP SEVERE message test");
        
        // Log with specific levels
        plugin.getLogger().log(Level.INFO, "DEBUG: CloneSMP explicit INFO level test");
        plugin.getLogger().log(Level.WARNING, "DEBUG: CloneSMP explicit WARNING level test");
        plugin.getLogger().log(Level.SEVERE, "DEBUG: CloneSMP explicit SEVERE level test");
        
        // Direct console messages
        Bukkit.getConsoleSender().sendMessage("DEBUG: CloneSMP direct console message test");
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fColored debug message test");
        Bukkit.getConsoleSender().sendMessage("§c[CloneSMP] §4RED §fcolored debug message test");
        
        // Log exception
        try {
            throw new Exception("Test exception for logging");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "DEBUG: CloneSMP exception test", e);
        }
        
        // Print plugin info
        Bukkit.getConsoleSender().sendMessage("§e====== §a[CloneSMP DEBUG] §e======");
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fPlugin version: §e" + plugin.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fServer version: §e" + Bukkit.getVersion());
        Bukkit.getConsoleSender().sendMessage("§a[CloneSMP] §fBukkit version: §e" + Bukkit.getBukkitVersion());
        
        sender.sendMessage(ChatColor.GREEN + "Debug messages sent! Check your console.");
    }
    
    /**
     * Send the help message to a command sender
     * 
     * @param sender The command sender
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== CloneSMP Commands =====");
        sender.sendMessage(ChatColor.YELLOW + "/clonesmp lives [player]" + ChatColor.WHITE + " - Check remaining lives");
        
        if (sender.hasPermission("clonesmp.admin.reload")) {
            sender.sendMessage(ChatColor.YELLOW + "/clonesmp reload" + ChatColor.WHITE + " - Reload configuration");
        }
        
        if (sender.hasPermission("clonesmp.admin.reset")) {
            sender.sendMessage(ChatColor.YELLOW + "/clonesmp reset <player>" + ChatColor.WHITE + " - Reset a player's lives");
        }
        
        if (sender.hasPermission("clonesmp.admin.debug")) {
            sender.sendMessage(ChatColor.YELLOW + "/clonesmp debug" + ChatColor.WHITE + " - Send debug messages to console");
        }
        
        sender.sendMessage(ChatColor.GOLD + "=========================");
    }
    
    /**
     * Show a player their own lives
     * 
     * @param sender The command sender
     * @param player The player to check lives for
     */
    private void showLives(CommandSender sender, Player player) {
        UUID uuid = player.getUniqueId();
        int deaths = plugin.getData().getInt("deaths." + uuid, 0);
        int maxLives = plugin.getMaxLives();
        int livesLeft = Math.max(0, maxLives - deaths);
        
        sender.sendMessage(ChatColor.GOLD + "Lives: " + ChatColor.WHITE + livesLeft + "/" + maxLives);
    }
    
    /**
     * Show a player's lives to the command sender
     * 
     * @param sender The command sender
     * @param target The target player
     */
    private void showPlayerLives(CommandSender sender, OfflinePlayer target) {
        UUID uuid = target.getUniqueId();
        int deaths = plugin.getData().getInt("deaths." + uuid, 0);
        int maxLives = plugin.getMaxLives();
        int livesLeft = Math.max(0, maxLives - deaths);
        
        sender.sendMessage(ChatColor.GOLD + target.getName() + "'s lives: " + 
                          ChatColor.WHITE + livesLeft + "/" + maxLives);
    }
    
    /**
     * Reset a player's lives
     * 
     * @param sender The command sender
     * @param playerName The player name to reset
     */
    private void resetPlayerLives(CommandSender sender, String playerName) {
        // First, try to find an online player
        Player onlineTarget = Bukkit.getPlayer(playerName);
        if (onlineTarget != null) {
            resetLives(sender, onlineTarget);
            return;
        }
        
        // Try to parse as UUID
        try {
            UUID targetUUID = UUID.fromString(playerName);
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetUUID);
            if (offlineTarget.hasPlayedBefore()) {
                resetLives(sender, offlineTarget);
                return;
            }
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, continue to search by name
        }
        
        // Search through offline players
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            if (offlinePlayer.getName() != null && 
                offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                resetLives(sender, offlinePlayer);
                return;
            }
        }
        
        sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
    }
    
    /**
     * Helper method to reset lives for a player
     * 
     * @param sender The command sender
     * @param target The target player
     */
    private void resetLives(CommandSender sender, OfflinePlayer target) {
        UUID uuid = target.getUniqueId();
        plugin.getData().set("deaths." + uuid, 0);
        plugin.saveData();
        
        sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s lives!");
        
        // If player is online, notify them
        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null && onlineTarget.isOnline()) {
            onlineTarget.sendMessage(ChatColor.GREEN + "Your lives have been reset by an admin!");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> commands = Arrays.asList("lives", "help", "reload", "reset");
            
            if (sender.hasPermission("clonesmp.admin.debug")) {
                commands = Arrays.asList("lives", "help", "reload", "reset", "debug");
            }
            
            return filterCompletions(commands, args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("lives") || args[0].equalsIgnoreCase("reset")) {
                return null; // Return null for player name list (Bukkit handles this automatically)
            }
        }
        
        return List.of(); // Empty list for no suggestions
    }
    
    /**
     * Filter a list of completions based on the current argument
     * 
     * @param completions The list of possible completions
     * @param current The current argument
     * @return A filtered list of completions
     */
    private List<String> filterCompletions(List<String> completions, String current) {
        return completions.stream()
            .filter(s -> s.startsWith(current.toLowerCase()))
            .toList();
    }
}