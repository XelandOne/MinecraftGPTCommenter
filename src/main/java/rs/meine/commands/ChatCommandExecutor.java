package rs.meine.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import rs.meine.services.OpenAIService;

public class ChatCommandExecutor implements CommandExecutor {
    private final JavaPlugin plugin;
    private final OpenAIService openAIService;
    
    public ChatCommandExecutor(JavaPlugin plugin, OpenAIService openAIService) {
        this.plugin = plugin;
        this.openAIService = openAIService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " <message>");
            return true;
        }
        
        String prompt = String.join(" ", args);
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String response = openAIService.generateSimpleResponse(prompt);
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                sender.sendMessage(response);
                
                // Add to chat history if sender is a player
                if (sender instanceof Player player) {
                    openAIService.addMessageToHistory(player.getUniqueId(), player.getName(), prompt);
                    openAIService.addMessageToHistory(player.getUniqueId(), "AI", response);
                }
            });
        });
        
        return true;
    }
} 