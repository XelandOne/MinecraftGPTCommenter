#!/bin/bash

# Colors for terminal output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Starting MinecraftGPTCommenter build and deployment...${NC}"
echo -e "${BLUE}Running: mvn clean package install -Pdeploy${NC}"
echo ""

# Run the Maven command
mvn clean package install -Pdeploy

# Check if the command was successful
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}Build and deployment completed successfully!${NC}"
else
    echo ""
    echo -e "\033[0;31mBuild or deployment failed. See above for details.${NC}"
    exit 1
fi 