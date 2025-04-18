package rs.meine.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import rs.meine.Main;
import rs.meine.config.ConfigManager;
import rs.meine.services.OpenAIService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles administrative commands for the plugin
 */
public class AdminCommandExecutor implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final ConfigManager configManager;
    private final OpenAIService openAIService;
    
    private static final List<String> SUBCOMMANDS = Arrays.asList(
        "reload", "status", "reset", "help"
    );
    
    /**
     * Creates a new AdminCommandExecutor
     * @param plugin The plugin instance
     * @param configManager The configuration manager
     * @param openAIService The OpenAI service
     */
    public AdminCommandExecutor(Main plugin, ConfigManager configManager, OpenAIService openAIService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.openAIService = openAIService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("minecraftgptcommenter.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                reloadConfig(sender);
                break;
            case "status":
                showStatus(sender);
                break;
            case "reset":
                if (args.length > 1 && args[1].equalsIgnoreCase("chat") && sender instanceof Player) {
                    resetPlayerChat((Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /gptadmin reset chat");
                }
                break;
            case "help":
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * Reloads the plugin configuration
     * @param sender The command sender
     */
    private void reloadConfig(CommandSender sender) {
        try {
            plugin.reloadConfig();
            configManager.reloadConfig();
            openAIService.initializeClient();
            
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
        }
    }
    
    /**
     * Shows the current status of the plugin
     * @param sender The command sender
     */
    private void showStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== MinecraftGPTCommenter Status ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "OpenAI Model: " + ChatColor.WHITE + configManager.getModel());
        sender.sendMessage(ChatColor.YELLOW + "API Key Configured: " + ChatColor.WHITE + 
            (!configManager.getApiKey().isEmpty() ? "Yes" : "No"));
        
        // Show feature status
        sender.sendMessage(ChatColor.YELLOW + "Features:");
        sender.sendMessage(ChatColor.YELLOW + "  - Player Join: " + 
            (configManager.isFeatureEnabled("player_join") ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        sender.sendMessage(ChatColor.YELLOW + "  - Player Death: " + 
            (configManager.isFeatureEnabled("player_death") ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        sender.sendMessage(ChatColor.YELLOW + "  - Player Chat: " + 
            (configManager.isFeatureEnabled("player_chat") ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
    }
    
    /**
     * Resets the chat history for a player
     * @param player The player
     */
    private void resetPlayerChat(Player player) {
        openAIService.clearChatHistory(player.getUniqueId());
        openAIService.resetRateLimit(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Your chat history and rate limits have been reset.");
    }
    
    /**
     * Shows help information
     * @param sender The command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== MinecraftGPTCommenter Admin Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/gptadmin reload" + ChatColor.WHITE + " - Reloads the plugin configuration");
        sender.sendMessage(ChatColor.YELLOW + "/gptadmin status" + ChatColor.WHITE + " - Shows the current plugin status");
        sender.sendMessage(ChatColor.YELLOW + "/gptadmin reset chat" + ChatColor.WHITE + " - Resets your chat history");
        sender.sendMessage(ChatColor.YELLOW + "/gptadmin help" + ChatColor.WHITE + " - Shows this help message");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("minecraftgptcommenter.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            String partialCommand = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                .filter(cmd -> cmd.startsWith(partialCommand))
                .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            String partialArg = args[1].toLowerCase();
            List<String> resetOptions = Arrays.asList("chat");
            return resetOptions.stream()
                .filter(opt -> opt.startsWith(partialArg))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 