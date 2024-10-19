export PATH=$PATH:${PWD}/../bin
export FABRIC_CA_CLIENT_HOME=${PWD}/organizations/peerOrganizations/registry.node.connection

mkdir -p organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection

fabric-ca-client register \
  --caname ca-registry \
  --id.name peer1 \
  --id.secret peer1pw \
  --id.type peer \
  --tls.certfiles ${PWD}/organizations/fabric-ca/registry/ca-cert.pem

fabric-ca-client enroll \
  -u https://peer1:peer1pw@localhost:7054 \
  --caname ca-registry \
  -M ${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection/msp \
  --tls.certfiles ${PWD}/organizations/fabric-ca/registry/ca-cert.pem

cp ${PWD}/organizations/peerOrganizations/registry.node.connection/msp/config.yaml ${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection/msp/config.yaml

fabric-ca-client enroll \
  -u https://peer1:peer1pw@localhost:7054 \
  --caname ca-registry \
  -M ${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection/tls \
  --enrollment.profile tls \
  --csr.hosts peer1.registry.node.connection \
  --csr.hosts localhost \
  --tls.certfiles ${PWD}/organizations/fabric-ca/registry/ca-cert.pem

cp ${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection/tls/tlscacerts/* ${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection/tls/ca.crt
cp ${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection/tls/signcerts/* ${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection/tls/server.crt
cp ${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection/tls/keystore/* ${PWD}/organizations/peerOrganizations/registry.node.connection/peers/peer1.registry.node.connection/tls/server.key
