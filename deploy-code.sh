#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

DEFAULT_CHANNEL_NAME="busan-headquarters-office"
DEFAULT_LANGUAGE="go"

CHANNEL_NAME="$DEFAULT_CHANNEL_NAME"
CHAINCODE_NAME=""
CHAINCODE_DIR=""
LANGUAGE="$DEFAULT_LANGUAGE"
CHAINCODE_VERSION="1.0.0"
COLLECTIONS_CONFIG=""

while [[ "$#" -gt 0 ]]; do
    case $1 in
        -c|--channel)
            CHANNEL_NAME="$2"
            shift 2
            ;;
        -ccn|--chaincode-name)
            CHAINCODE_NAME="$2"
            shift 2
            ;;
        -ccp|--chaincode-path)
            CHAINCODE_DIR="$2"
            shift 2
            ;;
        -ccl|--language)
            LANGUAGE="$2"
            shift 2
            ;;
        -ccv|--chaincode-version)
            CHAINCODE_VERSION="$2"
            shift 2
            ;;
        -cccg|--collections-config)
            COLLECTIONS_CONFIG="$2"
            shift 2
            ;;
        *)
            echo -e "${RED}Unknown parameter passed: $1${NC}"
            exit 1
            ;;
    esac
done

if [[ "$CHAINCODE_DIR" != /* ]]; then
    CHAINCODE_DIR="$(pwd)/$CHAINCODE_DIR"
fi

if [[ -n "$COLLECTIONS_CONFIG" && "$COLLECTIONS_CONFIG" != /* ]]; then
    COLLECTIONS_CONFIG="$(pwd)/chain-code/$CHAINCODE_NAME/$COLLECTIONS_CONFIG"
fi

echo -e "${BLUE}Parsed values:${NC}"
echo -e "${BLUE}CHANNEL_NAME: $CHANNEL_NAME${NC}"
echo -e "${BLUE}CHAINCODE_NAME: $CHAINCODE_NAME${NC}"
echo -e "${BLUE}CHAINCODE_DIR: $CHAINCODE_DIR${NC}"
echo -e "${BLUE}LANGUAGE: $LANGUAGE${NC}"
echo -e "${BLUE}CHAINCODE_VERSION: $CHAINCODE_VERSION${NC}"
echo -e "${BLUE}COLLECTIONS_CONFIG: $COLLECTIONS_CONFIG${NC}"

if [ -z "$CHAINCODE_NAME" ] || [ -z "$CHAINCODE_DIR" ]; then
    echo -e "${RED}Error: Chaincode name and directory are required.${NC}"
    exit 1
fi

if [ ! -x "./network/node-connection-network/network.sh" ]; then
    echo -e "${RED}Error: network.sh not found or not executable.${NC}"
    exit 1
fi

if [[ -n "$COLLECTIONS_CONFIG" ]]; then
    ./network/node-connection-network/network.sh deployCC -c "$CHANNEL_NAME" -ccn "$CHAINCODE_NAME" -ccp "$CHAINCODE_DIR" -ccl "$LANGUAGE" -ccv "$CHAINCODE_VERSION" -cccg "$COLLECTIONS_CONFIG"
else
    ./network/node-connection-network/network.sh deployCC -c "$CHANNEL_NAME" -ccn "$CHAINCODE_NAME" -ccp "$CHAINCODE_DIR" -ccl "$LANGUAGE" -ccv "$CHAINCODE_VERSION"
fi
