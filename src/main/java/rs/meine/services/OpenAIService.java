package rs.meine.services;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import rs.meine.config.ConfigManager;
import rs.meine.models.ChatMessage;
import rs.meine.models.RateLimitInfo;

public class OpenAIService {
    private OpenAIClient client;
    private final ConfigManager configManager;
    private final Map<UUID, LinkedList<ChatMessage>> chatHistory = new HashMap<>();
    private final Map<UUID, RateLimitInfo> rateLimits = new ConcurrentHashMap<>();
    private final Logger logger;
    private boolean isInitialized = false;
    
    public OpenAIService(ConfigManager configManager) {
        this.configManager = configManager;
        this.logger = Logger.getLogger(OpenAIService.class.getName());
        initializeClient();
    }
    
    /**
     * Initializes the OpenAI client with current configuration
     * @return true if initialization was successful
     */
    public boolean initializeClient() {
        String apiKey = configManager.getApiKey();
        
        if (apiKey == null || apiKey.isEmpty()) {
            logger.severe("OpenAI API key is not configured. The plugin will not function correctly.");
            isInitialized = false;
            return false;
        }
        
        try {
            this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(configManager.getConnectionTimeout()))
                .build();
            isInitialized = true;
            logger.info("OpenAI client initialized successfully");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize OpenAI client", e);
            isInitialized = false;
            return false;
        }
    }
    
    /**
     * Generates a simple response to a prompt
     * @param prompt The prompt to send to the model
     * @return The AI-generated response or error message
     */
    public String generateSimpleResponse(String prompt) {
        if (!isInitialized || client == null) {
            return "Error: OpenAI service is not properly initialized. Check server logs.";
        }
        
        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage(prompt)
                .model(configManager.getModel())
                .temperature(configManager.getTemperature())
                .maxCompletionTokens(configManager.getMaxTokens())
                .build();
            
            ChatCompletion chatCompletion = client.chat().completions().create(params);
            return chatCompletion.choices().get(0).message().content().orElse("No response generated");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error generating simple response", e);
            return "Sorry, I encountered an error processing your request. Please try again later.";
        }
    }
    
    /**
     * Checks if a player is rate limited
     * @param playerUUID The UUID of the player to check
     * @return true if the player is currently rate limited
     */
    public boolean isRateLimited(UUID playerUUID) {
        RateLimitInfo info = rateLimits.get(playerUUID);
        if (info == null) {
            return false;
        }
        
        Instant now = Instant.now();
        if (now.isAfter(info.getResetTime())) {
            rateLimits.remove(playerUUID);
            return false;
        }
        
        if (info.getRemainingRequests() <= 0) {
            return true;
        }
        
        info.decrementRemainingRequests();
        return false;
    }
    
    /**
     * Generates a response to a player's chat message with context
     * @param playerName The name of the player
     * @param message The message sent by the player
     * @param event The player chat event
     * @return The AI-generated response or error message
     */
    public String generatePlayerChatResponse(String playerName, String message, AsyncPlayerChatEvent event) {
        if (!isInitialized || client == null) {
            return "Error: OpenAI service is not properly initialized. Check server logs.";
        }
        
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Check rate limits
        if (isRateLimited(playerUUID)) {
            RateLimitInfo info = rateLimits.get(playerUUID);
            long secondsRemaining = Duration.between(Instant.now(), info.getResetTime()).getSeconds();
            return "Rate limit reached. Please try again in " + secondsRemaining + " seconds.";
        }
        
        // Initialize rate limit for new users
        if (!rateLimits.containsKey(playerUUID)) {
            rateLimits.put(playerUUID, new RateLimitInfo(
                configManager.getRequestsPerWindow(),
                Instant.now().plus(Duration.ofSeconds(configManager.getRateLimitWindowSeconds()))
            ));
        }
        
        try {
            // Gather world and server information
            String worldName = player.getWorld().getName();
            int onlinePlayers = player.getServer().getOnlinePlayers().size();
            String serverVersion = player.getServer().getVersion();
            String timeOfDay = player.getWorld().getTime() < 12000 ? "day" : "night";
            String biome = player.getLocation().getBlock().getBiome().toString();
            
            // Generate inventory summary
            String inventorySummary = getInventorySummary(player);
            
            // Get chat history for this player
            String chatHistorySummary = getChatHistorySummary(playerUUID);
            
            String systemPrompt = String.format(
                "You are a helpful AI assistant inside a Minecraft server. " +
                "You're conversing with %s. " +
                "Current server details: %d players online, running %s. " +
                "World '%s' is currently experiencing %s time. " +
                "Player is in %s biome. " +
                "Player's inventory: %s. " +
                "Recent conversation: %s " +
                "Be concise, funny, and helpful.",
                playerName, onlinePlayers, serverVersion, worldName, timeOfDay, biome, 
                inventorySummary, chatHistorySummary
            );
            
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addSystemMessage(systemPrompt)
                .addUserMessage(message)
                .model(configManager.getModel())
                .temperature(configManager.getTemperature())
                .maxCompletionTokens(configManager.getMaxTokens())
                .build();
            
            ChatCompletion chatCompletion = client.chat().completions().create(params);
            String response = chatCompletion.choices().get(0).message().content().orElse("No response generated");
            
            // Add to chat history
            addMessageToHistory(playerUUID, "Player", message);
            addMessageToHistory(playerUUID, "AI", response);
            
            return response;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error generating player chat response", e);
            return "Sorry, I encountered an error processing your request. Please try again later.";
        }
    }
    
    /**
     * Adds a message to the player's chat history
     * @param playerUUID The UUID of the player
     * @param sender The sender of the message
     * @param content The content of the message
     */
    public void addMessageToHistory(UUID playerUUID, String sender, String content) {
        chatHistory.putIfAbsent(playerUUID, new LinkedList<>());
        LinkedList<ChatMessage> playerHistory = chatHistory.get(playerUUID);
        
        playerHistory.add(new ChatMessage(sender, content));
        
        // Keep only the last MAX_HISTORY messages
        while (playerHistory.size() > configManager.getMaxHistory()) {
            playerHistory.removeFirst();
        }
    }
    
    /**
     * Gets a summary of the player's inventory
     * @param player The player
     * @return A string summary of the player's inventory
     */
    private String getInventorySummary(Player player) {
        try {
            Map<String, Integer> itemCount = new HashMap<>();
            
            Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .forEach(item -> {
                    String itemName = item.getType().toString().toLowerCase().replace('_', ' ');
                    itemCount.put(itemName, itemCount.getOrDefault(itemName, 0) + item.getAmount());
                });
            
            // Generate a summary string
            if (itemCount.isEmpty()) {
                return "empty";
            }
            
            return itemCount.entrySet().stream()
                .map(entry -> entry.getValue() + " " + entry.getKey())
                .collect(Collectors.joining(", "));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error generating inventory summary", e);
            return "unknown inventory";
        }
    }
    
    /**
     * Gets a summary of the player's chat history
     * @param playerUUID The UUID of the player
     * @return A string summary of the player's chat history
     */
    private String getChatHistorySummary(UUID playerUUID) {
        if (!chatHistory.containsKey(playerUUID) || chatHistory.get(playerUUID).isEmpty()) {
            return "No previous messages.";
        }
        
        return chatHistory.get(playerUUID).stream()
            .map(ChatMessage::toString)
            .collect(Collectors.joining(" | "));
    }
    
    /**
     * Clears the chat history for a player
     * @param playerUUID The UUID of the player
     */
    public void clearChatHistory(UUID playerUUID) {
        chatHistory.remove(playerUUID);
    }
    
    /**
     * Resets the rate limit for a player
     * @param playerUUID The UUID of the player
     */
    public void resetRateLimit(UUID playerUUID) {
        rateLimits.remove(playerUUID);
    }
} 