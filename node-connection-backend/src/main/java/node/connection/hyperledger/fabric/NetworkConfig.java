package node.connection.hyperledger.fabric;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class NetworkConfig {

    private String channelName;
    private List<Node> peers;
    private Node orderer;
    private Node node;

    private NetworkConfig(Builder builder) {
        channelName = builder.channelName;
        peers = builder.peers;
        orderer = builder.orderer;
        node = builder.node;
    }

    public void addPeer(FabricPeer peer) {
        this.peers.add(peer.toNode());
    }

    public static final class Builder {
        private String channelName;
        private List<Node> peers = new ArrayList<>();
        private Node orderer;
        private Node node;

        public Builder() {
        }

        public Builder channelName(String val) {
            channelName = val;
            return this;
        }

        public Builder peer(FabricPeer npeer) {
            peers.add(npeer.toNode());
            return this;
        }

        public Builder peer(String name, String url) {
            Node peer = Node.builder()
                    .name(name)
                    .url(url)
                    .build();
            peers.add(peer);
            return this;
        }

        public Builder orderer(FabricPeer npeer) {
            this.orderer = npeer.toNode();
            return this;
        }

        public Builder orderer(String name, String url) {
            orderer = Node.builder()
                    .name(name)
                    .url(url)
                    .build();
            return this;
        }

        public Builder node(String name, String url) {
            node = Node.builder()
                    .name(name)
                    .url(url)
                    .build();
            return this;
        }

        public Builder peer(Node val) {
            peers.add(val);
            return this;
        }

        public Builder orderer(Node val) {
            orderer = val;
            return this;
        }

        public Builder node(Node val) {
            node = val;
            return this;
        }

        public NetworkConfig build() {
            if (channelName == null) {
                throw new IllegalArgumentException("channelName is null");
            }
            if (peers == null || peers.isEmpty()) {
                throw new IllegalArgumentException("peers are null or empty");
            }
            if (orderer == null) {
                throw new IllegalArgumentException("orderer is null");
            }

            for (Node peer : peers) {
                peerProperties(peer.getProperties());
            }
            ordererProperties(orderer.getProperties());

            return new NetworkConfig(this);
        }

        public Properties baseProperties(Properties properties) {
            properties.put("grpc.keepAliveTime", new Object[]{1L, TimeUnit.MINUTES});
            properties.put("grpc.keepAliveTimeout", new Object[]{30L, TimeUnit.SECONDS});
            properties.put("grpc.keepAliveWithoutCalls", new Object[]{true});
            return properties;
        }

        public void peerProperties(Properties properties) {
            Properties peerProperties = baseProperties(properties);
            peerProperties.put("grpc.maxInboundMessageSize", 20000000);
        }

        public void ordererProperties(Properties properties) {
            Properties ordererProperties = baseProperties(properties);
            ordererProperties.put("ordererWaitTimeMilliSecs", "90000");
        }
    }
}