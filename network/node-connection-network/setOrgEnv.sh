#!/bin/bash
#
# SPDX-License-Identifier: Apache-2.0




# default to using Registry
ORG=${1:-Registry}

# Exit on first error, print all commands.
set -e
set -o pipefail

# Where am I?
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

echo "${DIR}"

ORDERER_CA=${DIR}/node-connection-network/organizations/ordererOrganizations/node.connection/tlsca/tlsca.node.connection-cert.pem
PEER0_REGISTRY_CA=${DIR}/node-connection-network/organizations/peerOrganizations/registry.node.connection/tlsca/tlsca.registry.node.connection-cert.pem
PEER0_VIEWER_CA=${DIR}/node-connection-network/organizations/peerOrganizations/viewer.node.connection/tlsca/tlsca.viewer.node.connection-cert.pem
PEER0_ORG3_CA=${DIR}/node-connection-network/organizations/peerOrganizations/org3.node.connection/tlsca/tlsca.org3.node.connection-cert.pem

if [[ ${ORG,,} == "Registry" || ${ORG,,} == "registry" || ${ORG,,} == "digibank" ]]; then

   CORE_PEER_LOCALMSPID=RegistryMSP
   CORE_PEER_MSPCONFIGPATH=${DIR}/node-connection-network/organizations/peerOrganizations/registry.node.connection/users/Admin@registry.node.connection/msp
   CORE_PEER_ADDRESS=localhost:7051
   CORE_PEER_TLS_ROOTCERT_FILE=${DIR}/node-connection-network/organizations/peerOrganizations/registry.node.connection/tlsca/tlsca.registry.node.connection-cert.pem

elif [[ ${ORG,,} == "Viewer" || ${ORG,,} == "viewer" || ${ORG,,} == "magnetocorp" ]]; then

   CORE_PEER_LOCALMSPID=ViewerMSP
   CORE_PEER_MSPCONFIGPATH=${DIR}/node-connection-network/organizations/peerOrganizations/viewer.node.connection/users/Admin@viewer.node.connection/msp
   CORE_PEER_ADDRESS=localhost:9051
   CORE_PEER_TLS_ROOTCERT_FILE=${DIR}/node-connection-network/organizations/peerOrganizations/viewer.node.connection/tlsca/tlsca.viewer.node.connection-cert.pem

else
   echo "Unknown \"$ORG\", please choose Registry/Digibank or Viewer/Magnetocorp"
   echo "For example to get the environment variables to set upa Viewer shell environment run:  ./setOrgEnv.sh Viewer"
   echo
   echo "This can be automated to set them as well with:"
   echo
   echo 'export $(./setOrgEnv.sh Viewer | xargs)'
   exit 1
fi

# output the variables that need to be set
echo "CORE_PEER_TLS_ENABLED=true"
echo "ORDERER_CA=${ORDERER_CA}"
echo "PEER0_REGISTRY_CA=${PEER0_REGISTRY_CA}"
echo "PEER0_VIEWER_CA=${PEER0_VIEWER_CA}"
echo "PEER0_ORG3_CA=${PEER0_ORG3_CA}"

echo "CORE_PEER_MSPCONFIGPATH=${CORE_PEER_MSPCONFIGPATH}"
echo "CORE_PEER_ADDRESS=${CORE_PEER_ADDRESS}"
echo "CORE_PEER_TLS_ROOTCERT_FILE=${CORE_PEER_TLS_ROOTCERT_FILE}"

echo "CORE_PEER_LOCALMSPID=${CORE_PEER_LOCALMSPID}"
