package rs.meine.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

/**
 * Manages plugin configuration
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final Logger logger;
    
    // Default values
    private static final String DEFAULT_API_KEY = "";
    private static final String DEFAULT_MODEL = "gpt-4o";
    private static final double DEFAULT_TEMPERATURE = 1.0;
    private static final int DEFAULT_MAX_HISTORY = 5;
    private static final int DEFAULT_MAX_TOKENS = 150;
    private static final int DEFAULT_REQUESTS_PER_WINDOW = 10;
    private static final int DEFAULT_RATE_LIMIT_WINDOW_SECONDS = 60;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10;
    
    /**
     * Creates a new ConfigManager
     * @param plugin The plugin instance
     */
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.logger = plugin.getLogger();
        
        // Set default config values
        initDefaultConfig();
    }
    
    /**
     * Initializes the default configuration
     */
    private void initDefaultConfig() {
        config.addDefault("openai.api_key", DEFAULT_API_KEY);
        config.addDefault("openai.model", DEFAULT_MODEL);
        config.addDefault("openai.temperature", DEFAULT_TEMPERATURE);
        config.addDefault("openai.max_tokens", DEFAULT_MAX_TOKENS);
        
        config.addDefault("chat.max_history", DEFAULT_MAX_HISTORY);
        
        config.addDefault("rate_limiting.requests_per_window", DEFAULT_REQUESTS_PER_WINDOW);
        config.addDefault("rate_limiting.window_seconds", DEFAULT_RATE_LIMIT_WINDOW_SECONDS);
        
        config.addDefault("timeouts.connection", DEFAULT_CONNECTION_TIMEOUT);
        
        config.addDefault("features.player_join", true);
        config.addDefault("features.player_death", true);
        config.addDefault("features.player_chat", true);
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
        
        // Validate configuration
        validateConfig();
    }
    
    /**
     * Validates the configuration for required values
     */
    private void validateConfig() {
        // Check API key
        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            logger.warning("No OpenAI API key found in config.yml or environment variables.");
            logger.warning("The plugin will not function correctly until an API key is provided.");
        }
    }
    
    /**
     * Gets the OpenAI API key
     * @return The API key
     */
    public String getApiKey() {
        String apiKey = config.getString("openai.api_key", "");
        
        // Try to get from environment variable if not set in config
        if (apiKey.isEmpty()) {
            apiKey = System.getenv("OPENAI_API_KEY");
        }
        
        return apiKey != null ? apiKey : "";
    }
    
    /**
     * Gets the OpenAI model to use
     * @return The model name
     */
    public String getModel() {
        return config.getString("openai.model", DEFAULT_MODEL);
    }
    
    /**
     * Gets the temperature for OpenAI requests
     * @return The temperature value
     */
    public double getTemperature() {
        return config.getDouble("openai.temperature", DEFAULT_TEMPERATURE);
    }
    
    /**
     * Gets the maximum number of tokens for OpenAI requests
     * @return The maximum number of tokens
     */
    public int getMaxTokens() {
        return config.getInt("openai.max_tokens", DEFAULT_MAX_TOKENS);
    }
    
    /**
     * Gets the maximum number of chat history messages to store per player
     * @return The maximum history size
     */
    public int getMaxHistory() {
        return config.getInt("chat.max_history", DEFAULT_MAX_HISTORY);
    }
    
    /**
     * Checks if a feature is enabled
     * @param feature The feature name
     * @return true if the feature is enabled
     */
    public boolean isFeatureEnabled(String feature) {
        return config.getBoolean("features." + feature, true);
    }
    
    /**
     * Gets the number of requests allowed per rate limit window
     * @return The number of requests
     */
    public int getRequestsPerWindow() {
        return config.getInt("rate_limiting.requests_per_window", DEFAULT_REQUESTS_PER_WINDOW);
    }
    
    /**
     * Gets the rate limit window size in seconds
     * @return The window size in seconds
     */
    public int getRateLimitWindowSeconds() {
        return config.getInt("rate_limiting.window_seconds", DEFAULT_RATE_LIMIT_WINDOW_SECONDS);
    }
    
    /**
     * Gets the connection timeout in seconds
     * @return The connection timeout
     */
    public int getConnectionTimeout() {
        return config.getInt("timeouts.connection", DEFAULT_CONNECTION_TIMEOUT);
    }
    
    /**
     * Reloads the configuration from disk
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        validateConfig();
    }
} 