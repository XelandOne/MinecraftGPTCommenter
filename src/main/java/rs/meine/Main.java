package rs.meine;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    OpenAiService service = new OpenAiService("YOUR_OPENAI_API_KEY ");

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

    @Override
    public void onEnable() {
        System.out.println("Hello world!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String deathMessage = event.getDeathMessage();
        String message = chatRequest("Generate a funny short message about the death of " + event.getEntity().getName() + " on the Minecraft server. He died because " + deathMessage);
        event.setDeathMessage(message);
    }



    private String chatRequest(String prompt) {
        CompletionRequest completionRequest = CompletionRequest.builder()
                .user("MinecraftGPTCommenter")
                .model("gpt-3.5-turbo")
                .prompt(prompt)
                .maxTokens(256)
                .temperature(1.35)
                .build();
        CompletionResult answer = service.createCompletion(completionRequest);
        String message = answer.getChoices().get(0).getText();
        if (message.startsWith("\"") && message.endsWith("\"")) {
            message = message.substring(1, message.length() - 1);
        }
        return message;
    }
}