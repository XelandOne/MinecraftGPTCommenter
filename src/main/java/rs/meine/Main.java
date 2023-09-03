package rs.meine;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static org.bukkit.Bukkit.getScheduler;

public class Main extends JavaPlugin implements Listener {
    OpenAiService service = new OpenAiService("YOUR_OPENAI_API_KEY ");

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

    @Override
    public void onEnable() {
        System.out.println("MinecraftGPTCommenter plugin enabled!");
        getServer().getPluginManager().registerEvents(this, this);
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
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        String message = chatRequest("Generate a short message about the advancement " + event.getAdvancement().getKey().getKey() + " of the player " + event.getPlayer().getName() + " on the Minecraft server.");
        getServer().broadcastMessage(message);
    }


    private String chatRequest(String prompt) {
        List<ChatMessage> messages = List.of(new ChatMessage(ChatMessageRole.USER.value(), prompt));
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .user("MinecraftGPTCommenter")
                .model("gpt-3.5-turbo")
                .messages(messages)
                .maxTokens(256)
                .temperature(1.35)
                .build();
        ChatCompletionResult answer = service.createChatCompletion(completionRequest);
        String message = answer.getChoices().get(0).getMessage().getContent();
        if (message.startsWith("\"") && message.endsWith("\"")) {
            message = message.substring(1, message.length() - 1);
        }
        return message;
    }
}