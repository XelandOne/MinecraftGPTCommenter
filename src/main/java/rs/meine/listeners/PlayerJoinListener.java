package rs.meine.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import rs.meine.services.OpenAIService;

public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final OpenAIService openAIService;
    
    public PlayerJoinListener(JavaPlugin plugin, OpenAIService openAIService) {
        this.plugin = plugin;
        this.openAIService = openAIService;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String message = openAIService.generateSimpleResponse(
                "Generate a short greeting of the player " + event.getPlayer().getName() + " on the Minecraft server."
            );
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getServer().broadcastMessage(message);
            });
        });
    }
} 