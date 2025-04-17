#!/bin/bash

# Plugin JAR file path
JAR_FILE="target/MinecraftGPTCommenter-1.0-SNAPSHOT.jar"

# Remote server
REMOTE_SERVER="oracle"

# Remote destination path
REMOTE_DEST="$REMOTE_SERVER:minecraft-docker/data/plugins/"

# Docker container name
CONTAINER_NAME="minecraft-docker-mc-1"

# Colors for terminal output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo "Starting plugin deployment..."

# Check if the JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}Error: JAR file not found at $JAR_FILE${NC}"
    echo "Make sure you've built the project using 'mvn package' first."
    exit 1
fi

# Copy the JAR file to the remote server
echo "Copying JAR file to $REMOTE_DEST..."
scp "$JAR_FILE" "$REMOTE_DEST"

# Check if the copy was successful
if [ $? -eq 0 ]; then
    echo -e "${YELLOW}Plugin copied successfully. Restarting Minecraft container...${NC}"
    
    # Restart the Docker container remotely
    ssh $REMOTE_SERVER "sudo docker restart $CONTAINER_NAME"
    
    # Check if the restart was successful
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Deployment successful!${NC}"
        echo "The plugin has been copied and Minecraft server has been restarted."
    else
        echo -e "${RED}Warning: Plugin was copied but container restart failed.${NC}"
        echo "You may need to restart the Minecraft server manually."
        exit 1
    fi
else
    echo -e "${RED}Deployment failed.${NC}"
    echo "There was an error copying the JAR file to $REMOTE_DEST"
    exit 1
fi 