#!/bin/bash

function createRegistry() {
  infoln "Enrolling the CA admin"
  mkdir -p organizations/peerOrganizations/registry.node.connection/

  export FABRIC_CA_CLIENT_HOME=${PWD}/organizations/peerOrganizations/registry.node.connection/

  set -x
  fabric-ca-client enroll -u https://admin:adminpw@localhost:7054 --caname ca-registry --tls.certfiles "${PWD}/organizations/fabric-ca/registry/ca-cert.pem"
  { set +x; } 2>/dev/null

  echo 'NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/localhost-7054-ca-registry.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/localhost-7054-ca-registry.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/localhost-7054-ca-registry.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/localhost-7054-ca-registry.pem
    OrganizationalUnitIdentifier: orderer' > "${PWD}/organizations/peerOrganizations/registry.node.connection/msp/config.yaml"

  # Since the CA serves as both the organization CA and TLS CA, copy the org's root cert that was generated by CA startup into the org level ca and tlsca directories

  # Copy registry's CA cert to registry's /msp/tlscacerts directory (for use in the channel MSP definition)
  mkdir -p "${PWD}/organizations/peerOrganizations/registry.node.connection/msp/tlscacerts"
  cp "${PWD}/organizations/fabric-ca/registry/ca-cert.pem" "${PWD}/organizations/peerOrganizations/registry.node.connection/msp/tlscacerts/ca.crt"

  # Copy registry's CA cert to registry's /tlsca directory (for use by clients)
  mkdir -p "${PWD}/organizations/peerOrganizations/registry.node.connection/tlsca"
  cp "${PWD}/organizations/fabric-ca/registry/ca-cert.pem" "${PWD}/organizations/peerOrganizations/registry.node.connection/tlsca/tlsca.registry.node.connection-cert.pem"

  # Copy registry's CA cert to registry's /ca directory (for use by clients)
  mkdir -p "${PWD}/organizations/peerOrganizations/registry.node.connection/ca"
  cp "${PWD}/organizations/fabric-ca/registry/ca-cert.pem" "${PWD}/organizations/peerOrganizations/registry.node.connection/ca/ca.registry.node.connection-cert.pem"

  infoln "Registering peer0"
  set -x
  fabric-ca-client register --caname ca-registry --id.name peer0 --id.secret peer0pw --id.type peer --tls.certfiles "${PWD}/organizations/fabric-ca/registry/ca-cert.pem"
  { set +x; } 2>/dev/null

  infoln "Registering user"
  set -x
  fabric-ca-client register --caname ca-registry --id.name user1 --id.secret user1pw --id.type client --tls.certfiles "${PWD}/organizations/fabric-ca/registry/ca-cert.pem"
  { set +x; } 2>/dev/null

  infoln "Registering the org admin"
  set -x
  fabric-ca-client register --caname ca-registry --id.name registryadmin --id.secret registryadminpw --id.type admin --tls.certfiles "${PWD}/organizations/fabric-ca/registry/ca-cert.pem"
  { set +x; } 2>/dev/null

  infoln "Generating the peer0 msp"
  set -x
  fabric-ca-client enroll -u https://peer0:peer0pw@localhost:7054 --caname ca-registry -M "${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer0.registry.node.connection/msp" --tls.certfiles "${PWD}/organizations/fabric-ca/registry/ca-cert.pem"
  { set +x; } 2>/dev/null

  cp "${PWD}/organizations/peerOrganizations/registry.node.connection/msp/config.yaml" "${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer0.registry.node.connection/msp/config.yaml"

  infoln "Generating the peer0-tls certificates, use --csr.hosts to specify Subject Alternative Names"
  set -x
  fabric-ca-client enroll -u https://peer0:peer0pw@localhost:7054 --caname ca-registry -M "${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer0.registry.node.connection/tls" --enrollment.profile tls --csr.hosts peer0.registry.node.connection --csr.hosts localhost --tls.certfiles "${PWD}/organizations/fabric-ca/registry/ca-cert.pem"
  { set +x; } 2>/dev/null

  # Copy the tls CA cert, server cert, server keystore to well known file names in the peer's tls directory that are referenced by peer startup config
  cp "${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer0.registry.node.connection/tls/tlscacerts/"* "${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer0.registry.node.connection/tls/ca.crt"
  cp "${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer0.registry.node.connection/tls/signcerts/"* "${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer0.registry.node.connection/tls/server.crt"
  cp "${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer0.registry.node.connection/tls/keystore/"* "${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer0.registry.node.connection/tls/server.key"

  infoln "Generating the user msp"
  set -x
  fabric-ca-client enroll -u https://user1:user1pw@localhost:7054 --caname ca-registry -M "${PWD}/organizations/peerOrganizations/registry.node.connection/users/User1@registry.node.connection/msp" --tls.certfiles "${PWD}/organizations/fabric-ca/registry/ca-cert.pem"
  { set +x; } 2>/dev/null

  cp "${PWD}/organizations/peerOrganizations/registry.node.connection/msp/config.yaml" "${PWD}/organizations/peerOrganizations/registry.node.connection/users/User1@registry.node.connection/msp/config.yaml"

  infoln "Generating the org admin msp"
  set -x
  fabric-ca-client enroll -u https://registryadmin:registryadminpw@localhost:7054 --caname ca-registry -M "${PWD}/organizations/peerOrganizations/registry.node.connection/users/Admin@registry.node.connection/msp" --tls.certfiles "${PWD}/organizations/fabric-ca/registry/ca-cert.pem"
  { set +x; } 2>/dev/null

  cp "${PWD}/organizations/peerOrganizations/registry.node.connection/msp/config.yaml" "${PWD}/organizations/peerOrganizations/registry.node.connection/users/Admin@registry.node.connection/msp/config.yaml"
}

function createViewer() {
  infoln "Enrolling the CA admin"
  mkdir -p organizations/peerOrganizations/viewer.node.connection/

  export FABRIC_CA_CLIENT_HOME=${PWD}/organizations/peerOrganizations/viewer.node.connection/

  set -x
  fabric-ca-client enroll -u https://admin:adminpw@localhost:8054 --caname ca-viewer --tls.certfiles "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem"
  { set +x; } 2>/dev/null

  echo 'NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/localhost-8054-ca-viewer.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/localhost-8054-ca-viewer.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/localhost-8054-ca-viewer.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/localhost-8054-ca-viewer.pem
    OrganizationalUnitIdentifier: orderer' > "${PWD}/organizations/peerOrganizations/viewer.node.connection/msp/config.yaml"

  # Since the CA serves as both the organization CA and TLS CA, copy the org's root cert that was generated by CA startup into the org level ca and tlsca directories

  # Copy viewer's CA cert to viewer's /msp/tlscacerts directory (for use in the channel MSP definition)
  mkdir -p "${PWD}/organizations/peerOrganizations/viewer.node.connection/msp/tlscacerts"
  cp "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem" "${PWD}/organizations/peerOrganizations/viewer.node.connection/msp/tlscacerts/ca.crt"

  # Copy viewer's CA cert to viewer's /tlsca directory (for use by clients)
  mkdir -p "${PWD}/organizations/peerOrganizations/viewer.node.connection/tlsca"
  cp "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem" "${PWD}/organizations/peerOrganizations/viewer.node.connection/tlsca/tlsca.viewer.node.connection-cert.pem"

  # Copy viewer's CA cert to viewer's /ca directory (for use by clients)
  mkdir -p "${PWD}/organizations/peerOrganizations/viewer.node.connection/ca"
  cp "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem" "${PWD}/organizations/peerOrganizations/viewer.node.connection/ca/ca.viewer.node.connection-cert.pem"

  infoln "Registering peer0"
  set -x
  fabric-ca-client register --caname ca-viewer --id.name peer0 --id.secret peer0pw --id.type peer --tls.certfiles "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem"
  { set +x; } 2>/dev/null

  infoln "Registering user"
  set -x
  fabric-ca-client register --caname ca-viewer --id.name user1 --id.secret user1pw --id.type client --tls.certfiles "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem"
  { set +x; } 2>/dev/null

  infoln "Registering the org admin"
  set -x
  fabric-ca-client register --caname ca-viewer --id.name vieweradmin --id.secret vieweradminpw --id.type admin --tls.certfiles "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem"
  { set +x; } 2>/dev/null

  infoln "Generating the peer0 msp"
  set -x
  fabric-ca-client enroll -u https://peer0:peer0pw@localhost:8054 --caname ca-viewer -M "${PWD}/organizations/peerOrganizations/viewer.node.connection/peers/peer0.viewer.node.connection/msp" --tls.certfiles "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem"
  { set +x; } 2>/dev/null

  cp "${PWD}/organizations/peerOrganizations/viewer.node.connection/msp/config.yaml" "${PWD}/organizations/peerOrganizations/viewer.node.connection/peers/peer0.viewer.node.connection/msp/config.yaml"

  infoln "Generating the peer0-tls certificates, use --csr.hosts to specify Subject Alternative Names"
  set -x
  fabric-ca-client enroll -u https://peer0:peer0pw@localhost:8054 --caname ca-viewer -M "${PWD}/organizations/peerOrganizations/viewer.node.connection/peers/peer0.viewer.node.connection/tls" --enrollment.profile tls --csr.hosts peer0.viewer.node.connection --csr.hosts localhost --tls.certfiles "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem"
  { set +x; } 2>/dev/null

  # Copy the tls CA cert, server cert, server keystore to well known file names in the peer's tls directory that are referenced by peer startup config
  cp "${PWD}/organizations/peerOrganizations/viewer.node.connection/peers/peer0.viewer.node.connection/tls/tlscacerts/"* "${PWD}/organizations/peerOrganizations/viewer.node.connection/peers/peer0.viewer.node.connection/tls/ca.crt"
  cp "${PWD}/organizations/peerOrganizations/viewer.node.connection/peers/peer0.viewer.node.connection/tls/signcerts/"* "${PWD}/organizations/peerOrganizations/viewer.node.connection/peers/peer0.viewer.node.connection/tls/server.crt"
  cp "${PWD}/organizations/peerOrganizations/viewer.node.connection/peers/peer0.viewer.node.connection/tls/keystore/"* "${PWD}/organizations/peerOrganizations/viewer.node.connection/peers/peer0.viewer.node.connection/tls/server.key"

  infoln "Generating the user msp"
  set -x
  fabric-ca-client enroll -u https://user1:user1pw@localhost:8054 --caname ca-viewer -M "${PWD}/organizations/peerOrganizations/viewer.node.connection/users/User1@viewer.node.connection/msp" --tls.certfiles "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem"
  { set +x; } 2>/dev/null

  cp "${PWD}/organizations/peerOrganizations/viewer.node.connection/msp/config.yaml" "${PWD}/organizations/peerOrganizations/viewer.node.connection/users/User1@viewer.node.connection/msp/config.yaml"

  infoln "Generating the org admin msp"
  set -x
  fabric-ca-client enroll -u https://vieweradmin:vieweradminpw@localhost:8054 --caname ca-viewer -M "${PWD}/organizations/peerOrganizations/viewer.node.connection/users/Admin@viewer.node.connection/msp" --tls.certfiles "${PWD}/organizations/fabric-ca/viewer/ca-cert.pem"
  { set +x; } 2>/dev/null

  cp "${PWD}/organizations/peerOrganizations/viewer.node.connection/msp/config.yaml" "${PWD}/organizations/peerOrganizations/viewer.node.connection/users/Admin@viewer.node.connection/msp/config.yaml"
}

function createOrderer() {
  infoln "Enrolling the CA admin"
  mkdir -p organizations/ordererOrganizations/node.connection

  export FABRIC_CA_CLIENT_HOME=${PWD}/organizations/ordererOrganizations/node.connection

  set -x
  fabric-ca-client enroll -u https://admin:adminpw@localhost:9054 --caname ca-orderer --tls.certfiles "${PWD}/organizations/fabric-ca/ordererOrg/ca-cert.pem"
  { set +x; } 2>/dev/null

  echo 'NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/localhost-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/localhost-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/localhost-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/localhost-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: orderer' > "${PWD}/organizations/ordererOrganizations/node.connection/msp/config.yaml"

  # Since the CA serves as both the organization CA and TLS CA, copy the org's root cert that was generated by CA startup into the org level ca and tlsca directories

  # Copy orderer org's CA cert to orderer org's /msp/tlscacerts directory (for use in the channel MSP definition)
  mkdir -p "${PWD}/organizations/ordererOrganizations/node.connection/msp/tlscacerts"
  cp "${PWD}/organizations/fabric-ca/ordererOrg/ca-cert.pem" "${PWD}/organizations/ordererOrganizations/node.connection/msp/tlscacerts/tlsca.node.connection-cert.pem"

  # Copy orderer org's CA cert to orderer org's /tlsca directory (for use by clients)
  mkdir -p "${PWD}/organizations/ordererOrganizations/node.connection/tlsca"
  cp "${PWD}/organizations/fabric-ca/ordererOrg/ca-cert.pem" "${PWD}/organizations/ordererOrganizations/node.connection/tlsca/tlsca.node.connection-cert.pem"

  infoln "Registering orderer"
  set -x
  fabric-ca-client register --caname ca-orderer --id.name orderer --id.secret ordererpw --id.type orderer --tls.certfiles "${PWD}/organizations/fabric-ca/ordererOrg/ca-cert.pem"
  { set +x; } 2>/dev/null

  infoln "Registering the orderer admin"
  set -x
  fabric-ca-client register --caname ca-orderer --id.name ordererAdmin --id.secret ordererAdminpw --id.type admin --tls.certfiles "${PWD}/organizations/fabric-ca/ordererOrg/ca-cert.pem"
  { set +x; } 2>/dev/null

  infoln "Generating the orderer msp"
  set -x
  fabric-ca-client enroll -u https://orderer:ordererpw@localhost:9054 --caname ca-orderer -M "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/msp" --tls.certfiles "${PWD}/organizations/fabric-ca/ordererOrg/ca-cert.pem"
  { set +x; } 2>/dev/null

  cp "${PWD}/organizations/ordererOrganizations/node.connection/msp/config.yaml" "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/msp/config.yaml"

  infoln "Generating the orderer-tls certificates, use --csr.hosts to specify Subject Alternative Names"
  set -x
  fabric-ca-client enroll -u https://orderer:ordererpw@localhost:9054 --caname ca-orderer -M "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/tls" --enrollment.profile tls --csr.hosts orderer.node.connection --csr.hosts localhost --tls.certfiles "${PWD}/organizations/fabric-ca/ordererOrg/ca-cert.pem"
  { set +x; } 2>/dev/null

  # Copy the tls CA cert, server cert, server keystore to well known file names in the orderer's tls directory that are referenced by orderer startup config
  cp "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/tls/tlscacerts/"* "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/tls/ca.crt"
  cp "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/tls/signcerts/"* "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/tls/server.crt"
  cp "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/tls/keystore/"* "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/tls/server.key"

  # Copy orderer org's CA cert to orderer's /msp/tlscacerts directory (for use in the orderer MSP definition)
  mkdir -p "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/msp/tlscacerts"
  cp "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/tls/tlscacerts/"* "${PWD}/organizations/ordererOrganizations/node.connection/orderers/orderer.node.connection/msp/tlscacerts/tlsca.node.connection-cert.pem"

  infoln "Generating the admin msp"
  set -x
  fabric-ca-client enroll -u https://ordererAdmin:ordererAdminpw@localhost:9054 --caname ca-orderer -M "${PWD}/organizations/ordererOrganizations/node.connection/users/Admin@node.connection/msp" --tls.certfiles "${PWD}/organizations/fabric-ca/ordererOrg/ca-cert.pem"
  { set +x; } 2>/dev/null

  cp "${PWD}/organizations/ordererOrganizations/node.connection/msp/config.yaml" "${PWD}/organizations/ordererOrganizations/node.connection/users/Admin@node.connection/msp/config.yaml"
}
