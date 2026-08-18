package rs.meine.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import rs.meine.services.OpenAIService;

import java.util.ArrayList;
import java.util.List;

public class CommandGeneratorExecutor implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final OpenAIService openAIService;
    
    public CommandGeneratorExecutor(JavaPlugin plugin, OpenAIService openAIService) {
        this.plugin = plugin;
        this.openAIService = openAIService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6Usage: §f/" + label + " <natural language command description>");
            return true;
        }
        
        String prompt = String.join(" ", args);
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String generatedCommand = openAIService.generateMinecraftCommand(prompt);
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (sender instanceof Player player) {
                    player.sendMessage("§6Generated command: §f" + generatedCommand);
                    
                    // Create a clickable command suggestion
                    net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent("§a[Click to Execute]");
                    message.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                        net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, 
                        generatedCommand
                    ));
                    message.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                        net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                        new net.md_5.bungee.api.chat.ComponentBuilder("Click to run: " + generatedCommand).create()
                    ));
                    player.spigot().sendMessage(message);
                    
                    // Add to chat history
                    openAIService.addMessageToHistory(player.getUniqueId(), player.getName(), "Generate command: " + prompt);
                    openAIService.addMessageToHistory(player.getUniqueId(), "AI", "Generated: " + generatedCommand);
                } else {
                    sender.sendMessage("Generated command: " + generatedCommand);
                    sender.sendMessage("Use the command in game: " + generatedCommand);
                }
            });
        });
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // For this command, we don't offer specific tab completions
        // since it's expecting natural language input
        return new ArrayList<>();
    }
} 