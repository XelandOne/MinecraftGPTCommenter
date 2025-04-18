package rs.meine.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import rs.meine.Main;
import rs.meine.services.OpenAIService;

/**
 * Listener for player quit events to clean up resources
 */
public class PlayerQuitListener implements Listener {
    private final Main plugin;
    private final OpenAIService openAIService;
    
    /**
     * Creates a new PlayerQuitListener
     * @param plugin The plugin instance
     * @param openAIService The OpenAI service
     */
    public PlayerQuitListener(Main plugin, OpenAIService openAIService) {
        this.plugin = plugin;
        this.openAIService = openAIService;
    }
    
    /**
     * Handles player quit events to clean up resources
     * @param event The player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clean up chat history and rate limit info
        openAIService.clearChatHistory(player.getUniqueId());
        openAIService.resetRateLimit(player.getUniqueId());
        
        plugin.getLogger().fine("Cleaned up resources for player: " + player.getName());
    }
} 