package rs.meine.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import rs.meine.services.OpenAIService;

public class PlayerDeathListener implements Listener {
    private final JavaPlugin plugin;
    private final OpenAIService openAIService;
    
    public PlayerDeathListener(JavaPlugin plugin, OpenAIService openAIService) {
        this.plugin = plugin;
        this.openAIService = openAIService;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String deathMessage = event.getDeathMessage();
        Player player = event.getEntity();
        int playerLevel = player.getLevel();
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String message = openAIService.generateSimpleResponse(
                "Generate a funny short message about the death of " + player.getName() + 
                " on the Minecraft server. Player died because " + deathMessage + 
                ". Player was at level " + playerLevel + "."
            );
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getServer().broadcastMessage(message);
            });
        });
    }
} 