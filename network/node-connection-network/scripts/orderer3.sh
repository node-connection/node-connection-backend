#!/bin/bash


channel_name=$1

export PATH=${ROOTDIR}/../bin:${PWD}/../bin:$PATH
export ORDERER_ADMIN_TLS_SIGN_CERT=${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer3.node.connection/tls/server.crt /dev/null 2>&1
export ORDERER_ADMIN_TLS_PRIVATE_KEY=${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer3.node.connection/tls/server.key > /dev/null 2>&1

osnadmin channel join --channelID ${channel_name} --config-block ./channel-artifacts/${channel_name}.block -o localhost:7057 --ca-file "$ORDERER_CA" --client-cert "$ORDERER_ADMIN_TLS_SIGN_CERT" --client-key "$ORDERER_ADMIN_TLS_PRIVATE_KEY" >> log.txt 2>&1