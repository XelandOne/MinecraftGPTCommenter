package rs.meine.listeners;

import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.plugin.java.JavaPlugin;
import rs.meine.services.OpenAIService;

public class PlayerAchievementListener implements Listener {
    private final JavaPlugin plugin;
    private final OpenAIService openAIService;

    public PlayerAchievementListener(JavaPlugin plugin, OpenAIService openAIService) {
        this.plugin = plugin;
        this.openAIService = openAIService;
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        Advancement advancement = event.getAdvancement();

        // Skip hidden/recipe advancements, which fire constantly and have no display info
        if (advancement.getDisplay() == null) {
            return;
        }

        Player player = event.getPlayer();
        String achievementName = advancement.getDisplay().getTitle();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String message = openAIService.generateSimpleResponse(
                "Generate a short congratulatory message celebrating " + player.getName() +
                " for earning the achievement \"" + achievementName + "\" on the Minecraft server."
            );

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getServer().broadcastMessage(message);
            });
        });
    }
}
