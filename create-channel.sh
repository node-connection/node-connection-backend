#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

CHANNEL_NAME=busan-headquarters-office
REGISTRY_CHAINCODE_VERSION=1.0.4
ISSUANCE_CHAINCODE_VERSION=1.0.1

while [[ "$#" -gt 0 ]]; do
    case $1 in
        -c|--channel)
            CHANNEL_NAME="$2"
            shift 2
            ;;
        -rcv|--registry-chaincode-version)
            REGISTRY_CHAINCODE_VERSION="$2"
            shift 2
            ;;
        -icv|--issuance-chaincode-version)
            ISSUANCE_CHAINCODE_VERSION="$2"
            shift 2
            ;;
        *)
            echo -e "${RED}Unknown parameter passed: $1${NC}"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}Create new channel ${CHANNEL_NAME}${NC}"

# Create channel
./network/node-connection-network/network.sh createChannel -c $CHANNEL_NAME

echo -e "${GREEN}Channel ${CHANNEL_NAME} created successfully${NC}"

echo -e "${BLUE}Deploy chaincode${NC}"
echo -e "${BLUE}- registry chaincode version: ${REGISTRY_CHAINCODE_VERSION}${NC}"
echo -e "${BLUE}- issuance chaincode version: ${ISSUANCE_CHAINCODE_VERSION}${NC}"

# Deploy chaincode
bash deploy-code.sh -c $CHANNEL_NAME -ccn issuance -ccp chain-code/issuance -ccv $ISSUANCE_CHAINCODE_VERSION -cccg collections-config.json
bash deploy-code.sh -c $CHANNEL_NAME -ccn registry -ccp chain-code/registry -ccv $REGISTRY_CHAINCODE_VERSION

echo -e "${GREEN}Chaincode deployed successfully${NC}"
