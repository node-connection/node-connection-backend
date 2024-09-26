#!/bin/bash

function start_network() {
    echo -e "${YELLOW}Execute hyperledger fabric...${NC}"
    ./network/node-connection-network/network.sh up -ca
    
    # echo -e "${YELLOW}Execute hyperledger indy...${NC}"
    # cd indy-sdk

    # if [[ "$OSTYPE" == "darwin"* ]]; then
    #     echo -e "${YELLOW}Detected macOS. Building Docker image with platform linux/amd64...${NC}"
    #     docker build --platform linux/amd64 -f ci/indy-pool.dockerfile -t indy_pool .
    # else
    #     echo -e "${YELLOW}Building Docker image without platform flag...${NC}"
    #     docker build -f ci/indy-pool.dockerfile -t indy_pool .
    # fi

    # docker run -itd --name indy_pool -p 9701-9708:9701-9708 indy_pool

    # cd ..
}

# Function to stop the network
function stop_network() {
    echo "Stopping node-connection network..."
    ./network/node-connection-network/network.sh down
    # docker stop indy_pool
    # docker rm indy_pool
}

# Check the first argument passed to the script
if [ "$1" == "up" ]; then
    start_network
elif [ "$1" == "down" ]; then
    stop_network
else
    echo "Invalid argument. Use 'up' to start the network and 'down' to stop the network."
    exit 1
fi
