# Node Connection

# Hyperledger Fabric and Indy Configuration
# Indy Wallet Storage
USER_WALLET_STORAGE=src/main/java/node/connection/wallet/user
COURT_WALLET_STORAGE=src/main/java/node/connection/wallet/court
export USER_WALLET_STORAGE COURT_WALLET_STORAGE

# Fabric CA Information
CA_NAME=ca-registry
CA_URL=http://localhost:7054
CA_ADMIN_NAME=admin
CA_ADMIN_PASSWORD=adminpw
CA_PEM=/home/seokwns/workspace/project/node-connection-backend/network/node-connection-network/organizations/fabric-ca/registry/ca-cert.pem
export CA_NAME CA_URL CA_ADMIN_NAME CA_ADMIN_PASSWORD CA_PEM

# Fabric User Information
USER_MSP=RegistryMSP
USER_AFFILIATION=org1.department1
export USER_MSP USER_AFFILIATION

# Fabric Organization Information
REGISTRY_PEER_NAME=peer0.registry.node.connection
REGISTRY_PEER_URL=grpcs://localhost:7051
REGISTRY_PEER_PEM=/home/seokwns/workspace/project/node-connection-backend/network/node-connection-network/organizations/peerOrganizations/registry.node.connection/tlsca/tlsca.registry.node.connection-cert.pem
export REGISTRY_PEER_NAME REGISTRY_PEER_URL REGISTRY_PEER_PEM

VIEWER_PEER_NAME=peer0.viewer.node.connection
VIEWER_PEER_URL=grpcs://localhost:9051
VIEWER_PEER_PEM=/home/seokwns/workspace/project/node-connection-backend/network/node-connection-network/organizations/peerOrganizations/viewer.node.connection/tlsca/tlsca.viewer.node.connection-cert.pem
export VIEWER_PEER_NAME VIEWER_PEER_URL VIEWER_PEER_PEM

ORDERER_NAME=orderer.node.connection
ORDERER_URL=grpcs://localhost:7050
ORDERER_PEM=/home/seokwns/workspace/project/node-connection-backend/network/node-connection-network/organizations/ordererOrganizations/node.connection/tlsca/tlsca.node.connection-cert.pem
export ORDERER_NAME ORDERER_URL ORDERER_PEM

# Fabric Channel Information
CHANNEL_NAME=node-connection-default-channel
export CHANNEL_NAME

