package rs.meine;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getScheduler;

public class Main extends JavaPlugin implements Listener {
    private OpenAIClient client = OpenAIOkHttpClient.builder()
        .apiKey("YOUR_OPENAI_API_KEY ")
        .build();
        
    // Store last 5 messages for each player
    private Map<UUID, LinkedList<ChatMessage>> chatHistory = new HashMap<>();
    private static final int MAX_HISTORY = 5;

    // Inner class to represent a chat message
    private static class ChatMessage {
        private final String sender;
        private final String content;
        
        public ChatMessage(String sender, String content) {
            this.sender = sender;
            this.content = content;
        }
        
        @Override
        public String toString() {
            return sender + ": " + content;
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

    @Override
    public void onEnable() {
        System.out.println("MinecraftGPTCommenter plugin enabled!");
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("chatgpt").setExecutor((sender, command, label, args) -> {
            if (args.length == 0) {
                sender.sendMessage("Usage: /chatgpt <message>");
                return true;
            }
            String prompt = String.join(" ", args);
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                String response = chatRequest(prompt);
                getServer().getScheduler().runTask(this, () -> sender.sendMessage(response));
            });
            return true;
        });
        this.getCommand("chat").setExecutor((sender, command, label, args) -> {
            if (args.length == 0) {
                sender.sendMessage("Usage: /chat <message>");
                return true;
            }
            String prompt = String.join(" ", args);
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                String response = chatRequest(prompt);
                getServer().getScheduler().runTask(this, () -> sender.sendMessage(response));
            });
            return true;
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String message = chatRequest("Generate a short greeting of the player " + event.getPlayer().getName() + " on the Minecraft server.");
        getServer().broadcastMessage(message);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String deathMessage = event.getDeathMessage();
        getScheduler().runTaskAsynchronously(this, () -> {
            String message = chatRequest("Generate a funny short message about the death of " + event.getEntity().getName() + " on the Minecraft server. He died because " + deathMessage);
            getScheduler().runTask(this, () -> {
                getServer().broadcastMessage(message);
            });
        });
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerMessage = event.getMessage();
        String playerName = event.getPlayer().getName();
        UUID playerUUID = event.getPlayer().getUniqueId();
        
        // Add player message to chat history
        addMessageToHistory(playerUUID, playerName, playerMessage);
        
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            String response = playerChatRequest(playerName, playerMessage, event);
            
            // Add AI response to chat history
            getServer().getScheduler().runTask(this, () -> {
                addMessageToHistory(playerUUID, "AI", response);
                getServer().broadcastMessage(response);
            });
        });
    }
    
    private void addMessageToHistory(UUID playerUUID, String sender, String content) {
        chatHistory.putIfAbsent(playerUUID, new LinkedList<>());
        LinkedList<ChatMessage> playerHistory = chatHistory.get(playerUUID);
        
        playerHistory.add(new ChatMessage(sender, content));
        
        // Keep only the last MAX_HISTORY messages
        while (playerHistory.size() > MAX_HISTORY) {
            playerHistory.removeFirst();
        }
    }

    private String playerChatRequest(String playerName, String message, AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Gather world and server information
        String worldName = player.getWorld().getName();
        int onlinePlayers = getServer().getOnlinePlayers().size();
        String serverVersion = getServer().getVersion();
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
            .model(ChatModel.GPT_4_1_NANO)
            .temperature(1.35)
            .build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        return chatCompletion.choices().get(0).message().content().orElse("");
    }
    
    private String getInventorySummary(Player player) {
        ItemStack[] items = player.getInventory().getContents();
        Map<String, Integer> itemCount = new HashMap<>();
        
        for (ItemStack item : items) {
            if (item != null) {
                String itemName = item.getType().toString().toLowerCase().replace('_', ' ');
                itemCount.put(itemName, itemCount.getOrDefault(itemName, 0) + item.getAmount());
            }
        }
        
        // Generate a summary string
        if (itemCount.isEmpty()) {
            return "empty";
        }
        
        return itemCount.entrySet().stream()
            .map(entry -> entry.getValue() + " " + entry.getKey())
            .collect(Collectors.joining(", "));
    }
    
    private String getChatHistorySummary(UUID playerUUID) {
        if (!chatHistory.containsKey(playerUUID) || chatHistory.get(playerUUID).isEmpty()) {
            return "No previous messages.";
        }
        
        return chatHistory.get(playerUUID).stream()
            .map(ChatMessage::toString)
            .collect(Collectors.joining(" | "));
    }

    /*
    @EventHandler
    public void onRainStart(WeatherChangeEvent event) {
        if (event.getWorld().getPlayers().isEmpty()) {
            return;
        }

        if (event.toWeatherState()) {
            String message = chatRequest("Generate a short message about the rain on the Minecraft server.");
            getServer().broadcastMessage(message);
        }
    }*/


    private String chatRequest(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
            .addUserMessage(prompt)
            .model(ChatModel.GPT_4_1)
            .temperature(1.35)
            .build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        return chatCompletion.choices().get(0).message().content().orElse("");
    }
}