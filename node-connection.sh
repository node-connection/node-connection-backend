#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

function setup_requirements() {
    echo -e "${YELLOW}Checking and installing required software...${NC}"

    # Determine the shell type
    SHELL_PROFILE=""
    case "$SHELL" in
        */bash)
            SHELL_PROFILE="$HOME/.bashrc"
            ;;
        */zsh)
            SHELL_PROFILE="$HOME/.zshrc"
            ;;
        *)
            echo -e "${RED}Unsupported shell: $SHELL${NC}"
            return 1
            ;;
    esac

    # Check if Go is installed
    if ! command -v go &> /dev/null
    then
        echo -e "${YELLOW}Go is not installed. Installing Go...${NC}"
        wget -q https://golang.org/dl/go1.19.1.linux-amd64.tar.gz
        sudo tar -xzf go1.19.1.linux-amd64.tar.gz -C /usr/local/
        # Set up Go environment variables
        if ! grep -q "export PATH=\$PATH:/usr/local/go/bin" "$SHELL_PROFILE"; then
            echo "export PATH=\$PATH:/usr/local/go/bin" >> "$SHELL_PROFILE"
            source "$SHELL_PROFILE"
        fi
        echo -e "${GREEN}Go has been installed successfully.${NC}"
    else
        echo -e "${GREEN}Go is already installed.${NC}"
    fi

    # Check if Node.js is installed
    if ! command -v node &> /dev/null
    then
        echo -e "${YELLOW}Node.js is not installed. Installing Node.js...${NC}"
        # Install NVM (Node Version Manager)
        wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.4/install.sh | bash
        # Load NVM script
        export NVM_DIR="$HOME/.nvm"
        [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
        
        # Install the latest LTS version of Node.js
        nvm install --lts
        nvm use --lts
        echo -e "${GREEN}Node.js has been installed successfully.${NC}"
    else
        echo -e "${GREEN}Node.js is already installed.${NC}"
    fi

    # Check if libindy is installed
    # if ! dpkg -l | grep -q libindy; then
    #     echo -e "${YELLOW}libindy is not installed. Installing libindy...${NC}"

    #     sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88

    #     sudo add-apt-repository "deb https://repo.sovrin.org/sdk/deb bionic master"

    #     sudo apt-get update

    #     sudo apt-get install -y libindy

    #     echo -e "${GREEN}libindy has been installed successfully.${NC}"
    # else
    #     echo -e "${GREEN}libindy is already installed.${NC}"
    # fi
}

function setup_hyperledger_fabric() {
    echo -e "${YELLOW}Checking and setting up Hyperledger Fabric...${NC}"

    if [ ! -d "./network/bin" ] || [ ! -d "./network/builders" ] || [ ! -d "./network/config" ]; then
        echo -e "${YELLOW}Required folders not found in ./network/. Proceeding with setup.${NC}"

        curl -sSLO https://raw.githubusercontent.com/hyperledger/fabric/main/scripts/install-fabric.sh
        chmod +x install-fabric.sh

        ./install-fabric.sh

        cp -r fabric-samples/bin ./network/
        cp -r fabric-samples/builders ./network/
        cp -r fabric-samples/config ./network/

        rm -rf fabric-samples
        rm -f install-fabric.sh

        echo -e "${GREEN}Hyperledger Fabric setup complete.${NC}"
    else
        echo -e "${BLUE}The required folders already exist in ./network/. No actions needed.${NC}"
    fi
}

function setup_hyperledger_indy() {
    echo -e "${YELLOW}Checking and setting up Hyperledger Indy...${NC}"

    if [ ! -d "./indy-sdk" ]; then
        echo -e "${YELLOW}The indy-sdk folder does not exist. Proceeding with cloning.${NC}"

        git clone https://github.com/hyperledger/indy-sdk.git

        echo -e "${GREEN}indy-sdk installation and Docker container setup are complete.${NC}"
    else
        echo -e "${BLUE}The indy-sdk folder already exists. No actions will be performed.${NC}"
    fi
}

function config_org_env() {
    echo -e "${YELLOW}Configuring organization environment...${NC}"

    CONFIG_FILE="hyperledger.organization.config.sh"
    rm -f $CONFIG_FILE

    {
        cd ./network/node-connection-network

        echo "export PATH=\$PATH:$(realpath ../bin)"
        echo "export FABRIC_CFG_PATH=$(realpath ../config)"

        while IFS= read -r line; do
            if [[ $line =~ ^([^=]+)=(.*)$ ]]; then
                var_name=${BASH_REMATCH[1]}
                var_value=${BASH_REMATCH[2]}
                echo "export $var_name=$var_value"
            fi
        done < <(bash setOrgEnv.sh Registry)

        # while IFS= read -r line; do
        #     if [[ $line =~ ^([^=]+)=(.*)$ ]]; then
        #         var_name=${BASH_REMATCH[1]}
        #         var_value=${BASH_REMATCH[2]}
        #         echo "export $var_name=$var_value"
        #     fi
        # done < <(./setOrgEnv.sh Viewer)

        cd ../..
    } > $CONFIG_FILE

    chmod +x $CONFIG_FILE

    echo -e "${GREEN}Configuration exported to $CONFIG_FILE${NC}"

    local rc_file

    # Determine the current shell
    case "$SHELL" in
        */bash)
            rc_file="$HOME/.bashrc"
            ;;
        */zsh)
            rc_file="$HOME/.zshrc"
            ;;
        *)
            echo -e "${RED}Unsupported shell: $SHELL${NC}"
            return 1
            ;;
    esac

    # Define the path to the configuration script
    current_dir=$(pwd)
    config_script="$current_dir/$CONFIG_FILE"

    # Check if the source command is already in the rc file
    if grep -q "source $config_script" "$rc_file"; then
        echo -e "${BLUE}Organization source command already exists in $rc_file.${NC}"
    else
        # Append the source command to the rc file
        echo "" >> "$rc_file"
        echo "# Organization source Node Connection Hyperledger configuration script" >> "$rc_file"
        echo "source $config_script" >> "$rc_file"
        echo -e "${GREEN}Added source command to $rc_file.${NC}"
    fi

    # Source the file to apply changes immediately
    case "$SHELL" in
        */bash)
            source "$HOME/.bashrc"
            echo -e "${GREEN}Sourced $HOME/.bashrc${NC}"
            ;;
        */zsh)
            zsh -c "source $HOME/.zshrc"
            echo -e "${GREEN}Sourced $HOME/.zshrc${NC}"
            ;;
        *)
            echo -e "${RED}Unsupported shell: $SHELL${NC}"
            return 1
            ;;
    esac
}

function export_config() {
    echo -e "${YELLOW}Exporting Hyperledger configuration...${NC}"

    current_dir=$(pwd)

    # Write the configuration to hyperledger.config.sh
    echo '# Node Connection' > hyperledger.config.sh
    echo '' >> hyperledger.config.sh
    echo '# Hyperledger Fabric Configuration' >> hyperledger.config.sh

    # Indy Wallet Storage
    # echo '# Indy Wallet Storage' >> hyperledger.config.sh
    # echo 'USER_WALLET_STORAGE=src/main/java/node/connection/wallet/user' >> hyperledger.config.sh
    # echo 'COURT_WALLET_STORAGE=src/main/java/node/connection/wallet/court' >> hyperledger.config.sh
    # echo 'export USER_WALLET_STORAGE COURT_WALLET_STORAGE' >> hyperledger.config.sh

    # Fabric Organization Path
    echo "FABRIC_ORG_PATH=$current_dir/network/node-connection-network/organizations" >> hyperledger.config.sh
    echo 'export FABRIC_ORG_PATH' >> hyperledger.config.sh

    # Fabric CA Information
    echo '' >> hyperledger.config.sh
    echo '# Fabric CA Information' >> hyperledger.config.sh
    echo 'CA_REGISTRY_NAME=ca-registry' >> hyperledger.config.sh
    echo 'CA_REGISTRY_URL=https://localhost:7054' >> hyperledger.config.sh
    echo "CA_REGISTRY_PEM=$current_dir/network/node-connection-network/organizations/fabric-ca/registry/ca-cert.pem" >> hyperledger.config.sh
    echo 'CA_VIEWER_NAME=ca-viewer' >> hyperledger.config.sh
    echo 'CA_VIEWER_URL=https://localhost:8054' >> hyperledger.config.sh
    echo "CA_VIEWER_PEM=$current_dir/network/node-connection-network/organizations/fabric-ca/viewer/ca-cert.pem" >> hyperledger.config.sh
    echo 'CA_ADMIN_NAME=admin' >> hyperledger.config.sh
    echo 'CA_ADMIN_PASSWORD=adminpw' >> hyperledger.config.sh
    echo 'export CA_REGISTRY_NAME CA_REGISTRY_URL CA_REGISTRY_PEM CA_VIEWER_NAME CA_VIEWER_PEM CA_VIEWER_URL CA_ADMIN_NAME CA_ADMIN_PASSWORD' >> hyperledger.config.sh

    # Fabric User Information
    echo '' >> hyperledger.config.sh
    echo '# Fabric User Information' >> hyperledger.config.sh
    echo 'USER_MSP=RegistryMSP' >> hyperledger.config.sh
    echo 'USER_AFFILIATION=org1.department1' >> hyperledger.config.sh
    echo 'export USER_MSP USER_AFFILIATION' >> hyperledger.config.sh

    # Fabric Organization Information
    echo '' >> hyperledger.config.sh
    echo '# Fabric Organization Information' >> hyperledger.config.sh
    echo 'REGISTRY_PEER_NAME=peer0.registry.node.connection' >> hyperledger.config.sh
    echo 'REGISTRY_PEER_URL=grpcs://localhost:7051' >> hyperledger.config.sh
    echo "REGISTRY_PEER_PEM=$current_dir/network/node-connection-network/organizations/peerOrganizations/registry.node.connection/tlsca/tlsca.registry.node.connection-cert.pem" >> hyperledger.config.sh
    echo 'export REGISTRY_PEER_NAME REGISTRY_PEER_URL REGISTRY_PEER_PEM' >> hyperledger.config.sh

    echo '' >> hyperledger.config.sh
    echo 'VIEWER_PEER_NAME=peer0.viewer.node.connection' >> hyperledger.config.sh
    echo 'VIEWER_PEER_URL=grpcs://localhost:9051' >> hyperledger.config.sh
    echo "VIEWER_PEER_PEM=$current_dir/network/node-connection-network/organizations/peerOrganizations/viewer.node.connection/tlsca/tlsca.viewer.node.connection-cert.pem" >> hyperledger.config.sh
    echo 'export VIEWER_PEER_NAME VIEWER_PEER_URL VIEWER_PEER_PEM' >> hyperledger.config.sh

    echo '' >> hyperledger.config.sh
    echo 'ORDERER_NAME=orderer.node.connection' >> hyperledger.config.sh
    echo 'ORDERER_URL=grpcs://localhost:7050' >> hyperledger.config.sh
    echo "ORDERER_PEM=$current_dir/network/node-connection-network/organizations/ordererOrganizations/node.connection/tlsca/tlsca.node.connection-cert.pem" >> hyperledger.config.sh
    echo 'export ORDERER_NAME ORDERER_URL ORDERER_PEM' >> hyperledger.config.sh

    # Fabric Channel Information
    echo '' >> hyperledger.config.sh
    echo '# Fabric Channel Information' >> hyperledger.config.sh
    echo 'CHANNEL_NAME=busan-headquarters-office' >> hyperledger.config.sh
    echo 'export CHANNEL_NAME' >> hyperledger.config.sh

    echo '' >> hyperledger.config.sh

    # Make the script executable
    chmod +x hyperledger.config.sh

    local rc_file

    # Determine the current shell
    case "$SHELL" in
        */bash)
            rc_file="$HOME/.bashrc"
            ;;
        */zsh)
            rc_file="$HOME/.zshrc"
            ;;
        *)
            echo -e "${RED}Unsupported shell: $SHELL${NC}"
            return 1
            ;;
    esac

    # Define the path to the configuration script
    config_script="$current_dir/hyperledger.config.sh"

    # Check if the source command is already in the rc file
    if grep -q "source $config_script" "$rc_file"; then
        echo -e "${BLUE}Source command already exists in $rc_file.${NC}"
    else
        # Append the source command to the rc file
        echo "" >> "$rc_file"
        echo "# Source Node Connection Hyperledger configuration script" >> "$rc_file"
        echo "source $config_script" >> "$rc_file"
        echo -e "${GREEN}Added source command to $rc_file.${NC}"
    fi

    # Source the file to apply changes immediately
    case "$SHELL" in
        */bash)
            source "$HOME/.bashrc"
            echo -e "${GREEN}Sourced $HOME/.bashrc${NC}"
            ;;
        */zsh)
            zsh -c "source $HOME/.zshrc"
            echo -e "${GREEN}Sourced $HOME/.zshrc${NC}"
            ;;
        *)
            echo -e "${RED}Unsupported shell: $SHELL${NC}"
            return 1
            ;;
    esac
}

function export_backend_env() {
    local env_file="env.sh"
    
    if [[ -f $env_file ]]; then
        echo -e "${YELLOW}Found $env_file. Exporting environment variables...${NC}"
        
        # Source the env.sh file to export its variables
        source "$env_file"
        
        # Check if the environment variables are exported successfully
        echo -e "${GREEN}Environment variables from $env_file have been exported.${NC}"
        
        # Determine the current shell
        local rc_file
        case "$SHELL" in
            */bash)
                rc_file="$HOME/.bashrc"
                ;;
            */zsh)
                rc_file="$HOME/.zshrc"
                ;;
            *)
                echo -e "${RED}Unsupported shell: $SHELL${NC}"
                return 1
                ;;
        esac

        # Check if the source command is already in the rc file
        if grep -q "source $PWD/$env_file" "$rc_file"; then
            echo -e "${BLUE}Source command already exists in $rc_file.${NC}"
        else
            # Append the source command to the rc file
            echo "" >> "$rc_file"
            echo "# Source environment variables from $env_file" >> "$rc_file"
            echo "source $PWD/$env_file" >> "$rc_file"
            echo -e "${GREEN}Added source command to $rc_file.${NC}"
        fi

        # Source the file to apply changes immediately
        case "$SHELL" in
            */bash)
                source "$HOME/.bashrc"
                echo -e "${GREEN}Sourced $HOME/.bashrc${NC}"
                ;;
            */zsh)
                zsh -c "source $HOME/.zshrc"
                echo -e "${GREEN}Sourced $HOME/.zshrc${NC}"
                ;;
            *)
                echo -e "${RED}Unsupported shell: $SHELL${NC}"
                return 1
                ;;
        esac
    else
        echo -e "${RED}$env_file not found. No environment variables were exported.${NC}"
    fi
}

function start_application() {
    echo -e "${YELLOW}Starting node-connection application...${NC}"

    echo -e "${YELLOW}Checking requirements installation...${NC}"
    setup_requirements

    echo -e "${YELLOW}Checking Hyperledger installation...${NC}"
    setup_hyperledger_fabric
    # setup_hyperledger_indy

    echo -e "${YELLOW}Starting node-connection network...${NC}"
    ./node-connection-network.sh up

    echo -e "${YELLOW}Configuring node-connection network...${NC}"
    config_org_env

    echo -e "${YELLOW}Creating default channel...${NC}"
    ./network/node-connection-network/network.sh createChannel -c busan-headquarters-office

    echo -e "${YELLOW}Exporting Hyperledger network config...${NC}"
    export_config

    # TODO: 백엔드 서버 시작
    export_backend_env
    
    # TODO: 프론트엔드 서버 시작
    echo -e "${GREEN}Starting node-connection application complete.${NC}"
}

function stop_application() {
    echo -e "${YELLOW}Stopping node-connection network...${NC}"
    ./node-connection-network.sh down

    # TODO: 백엔드 서버 중지

    # TODO: 프론트엔드 서버 중지

    echo -e "${GREEN}Stopping node-connection application complete.${NC}"
}

if [ "$1" == "start" ]; then
    start_application
  
elif [ "$1" == "stop" ]; then
    stop_application
else
    echo -e "${RED}Invalid argument. Use 'start' to start the application and 'stop' to stop the application.${NC}"
    exit 1
fi