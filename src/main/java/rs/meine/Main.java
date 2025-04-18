package rs.meine;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;
import rs.meine.commands.AdminCommandExecutor;
import rs.meine.commands.ChatCommandExecutor;
import rs.meine.config.ConfigManager;
import rs.meine.listeners.PlayerChatListener;
import rs.meine.listeners.PlayerDeathListener;
import rs.meine.listeners.PlayerJoinListener;
import rs.meine.listeners.PlayerQuitListener;
import rs.meine.services.MetricsService;
import rs.meine.services.OpenAIService;

/**
 * Main plugin class for MinecraftGPTCommenter
 */
public class Main extends JavaPlugin {
    private ConfigManager configManager;
    private OpenAIService openAIService;
    private MetricsService metricsService;
    
    @Override
    public void onEnable() {
        try {
            // Initialize configuration
            saveDefaultConfig();
            configManager = new ConfigManager(this);
            
            // Initialize services
            openAIService = new OpenAIService(configManager);
            metricsService = new MetricsService(this);
            
            // Register event listeners
            if (configManager.isFeatureEnabled("player_join")) {
                getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, openAIService), this);
                getLogger().info("Player join listener registered");
            }
            
            if (configManager.isFeatureEnabled("player_death")) {
                getServer().getPluginManager().registerEvents(new PlayerDeathListener(this, openAIService), this);
                getLogger().info("Player death listener registered");
            }
            
            if (configManager.isFeatureEnabled("player_chat")) {
                getServer().getPluginManager().registerEvents(new PlayerChatListener(this, openAIService), this);
                getLogger().info("Player chat listener registered");
            }
            
            // Always register player quit listener to clean up resources
            getServer().getPluginManager().registerEvents(new PlayerQuitListener(this, openAIService), this);
            
            // Register commands
            registerCommands();
            
            getLogger().info("MinecraftGPTCommenter v" + getDescription().getVersion() + " enabled!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable MinecraftGPTCommenter plugin", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    /**
     * Registers all plugin commands
     */
    private void registerCommands() {
        // Chat commands
        ChatCommandExecutor chatCommandExecutor = new ChatCommandExecutor(this, openAIService);
        registerCommand("chatgpt", chatCommandExecutor);
        registerCommand("chat", chatCommandExecutor);
        
        // Admin commands
        AdminCommandExecutor adminCommandExecutor = new AdminCommandExecutor(this, configManager, openAIService);
        registerCommand("gptadmin", adminCommandExecutor);
    }
    
    /**
     * Registers a command with error handling
     * @param name The command name
     * @param executor The command executor
     */
    private void registerCommand(String name, Object executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor instanceof org.bukkit.command.CommandExecutor 
                ? (org.bukkit.command.CommandExecutor) executor 
                : null);
            
            if (executor instanceof org.bukkit.command.TabCompleter) {
                command.setTabCompleter((org.bukkit.command.TabCompleter) executor);
            }
            
            getLogger().info("Registered command: " + name);
        } else {
            getLogger().warning("Failed to register command: " + name + " (not found in plugin.yml)");
        }
    }
    
    /**
     * Gets the plugin's configuration manager
     * @return The configuration manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Gets the OpenAI service
     * @return The OpenAI service
     */
    public OpenAIService getOpenAIService() {
        return openAIService;
    }
    
    @Override
    public void onDisable() {
        // Gracefully shut down any ongoing operations
        if (openAIService != null) {
            // Nothing to do currently, but we could add shutdown logic here
        }
        
        getLogger().info("MinecraftGPTCommenter plugin disabled!");
    }
}