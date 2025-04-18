# MinecraftGPTCommenter

A Minecraft plugin that uses OpenAI's GPT models to generate dynamic and contextual responses to in-game events. The plugin can interact with players by responding to chat messages, generating welcome messages, and creating witty death messages.

## Features

- **Interactive Chat**: Players can chat with the AI using commands
- **Context-Aware**: Responses are informed by player inventory, location, and chat history
- **Multiple Event Handlers**:
  - Welcome messages when players join
  - Humorous commentary on player deaths
  - Responses to player chat messages
- **Admin Commands**: Easy management with reload, status, and reset commands
- **Rate Limiting**: Prevents excessive API usage
- **Error Handling**: Graceful failure and comprehensive logging

## Requirements

- Spigot or Paper server (1.19+)
- Java 17 or newer
- OpenAI API key

## Installation

1. Build the plugin by running 
2. Place the JAR file in your server's `plugins` directory
3. Start or restart your server
4. Configure your OpenAI API key in the `config.yml` file

## Configuration

The plugin creates a configuration file at `plugins/MinecraftGPTCommenter/config.yml` with the following options:

```yaml
# OpenAI API Configuration
openai:
  # Your OpenAI API key (can also be set via OPENAI_API_KEY environment variable)
  api_key: "YOUR_OPENAI_API_KEY"
  # Model to use for chat completions (e.g., gpt-4o, gpt-4-turbo, gpt-3.5-turbo)
  model: "gpt-4.1-nano"
  # Temperature controls randomness (0.0-2.0, higher is more random)
  temperature: 1.2
  # Maximum number of tokens in the response
  max_tokens: 150

# Chat Configuration
chat:
  # Maximum number of chat messages to keep in history per player
  max_history: 5

# Rate Limiting Configuration
rate_limiting:
  # Number of requests allowed per time window
  requests_per_window: 10
  # Time window in seconds
  window_seconds: 60

# Network Timeout Configuration (in seconds)
timeouts:
  # Connection timeout
  connection: 10

# Features toggle
features:
  # Generate welcome messages when players join
  player_join: true
  # Generate funny death messages when players die
  player_death: true
  # Respond to player chat messages
  player_chat: true
```

## Commands

### Player Commands

- `/chatgpt <message>` - Send a message to the AI
- `/chat <message>` - Alias for chatgpt command

### Admin Commands

- `/gptadmin reload` - Reload the plugin configuration
- `/gptadmin status` - Show the current plugin status
- `/gptadmin reset chat` - Reset your chat history
- `/gptadmin help` - Show the help message

## Permissions

- `minecraftgptcommenter.chat` - Allow use of chat commands (default: true)
- `minecraftgptcommenter.admin` - Allow use of admin commands (default: op)

## Building From Source

To build the plugin from source:

1. Clone the repository
2. Run `mvn clean package`
3. The JAR file will be in the `target` directory

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Releases

The plugin is automatically built and published to GitHub Releases using GitHub Actions when a new tag is pushed. To create a new release:

1. Tag the commit you want to release with a version number: `git tag v1.0.0`
2. Push the tag to GitHub: `git push origin v1.0.0`
3. The workflow will automatically build the JAR and create a GitHub Release

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Thanks to OpenAI for providing the API
- Built for Spigot/Paper Minecraft servers 