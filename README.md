# MinecraftGPTCommenter

A Minecraft plugin that integrates OpenAI's API to create an interactive and entertaining gameplay experience. The plugin generates contextual, humorous AI comments that respond to player actions, deaths, and conversations, making your Minecraft server feel more alive and dynamic.

## Setup

1. Make sure you have an OpenAI API key. You can obtain one from [OpenAI's website](https://platform.openai.com/api-keys).

2. Set the API key as an environment variable:
   ```
   export OPENAI_API_KEY=your_api_key_here
   ```
   For server configuration, add this to your startup script or system environment variables.


## Features

- Customized greeting messages when players join
- Funny AI comments on player deaths based on their inventory and level
- AI responses to player chat messages

## Commands

- `/chatgpt <message>` - Send a message to ChatGPT
- `/chat <message>` - Alias for chatgpt command 