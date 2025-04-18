package rs.meine.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import rs.meine.services.OpenAIService;

import java.util.UUID;

public class PlayerChatListener implements Listener {
    private final JavaPlugin plugin;
    private final OpenAIService openAIService;
    
    public PlayerChatListener(JavaPlugin plugin, OpenAIService openAIService) {
        this.plugin = plugin;
        this.openAIService = openAIService;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerMessage = event.getMessage();
        String playerName = event.getPlayer().getName();
        UUID playerUUID = event.getPlayer().getUniqueId();
        
        // Add player message to chat history
        openAIService.addMessageToHistory(playerUUID, playerName, playerMessage);
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String response = openAIService.generatePlayerChatResponse(playerName, playerMessage, event);
            
            // Add AI response to chat history
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                openAIService.addMessageToHistory(playerUUID, "AI", response);
                plugin.getServer().broadcastMessage(response);
            });
        });
    }
} 