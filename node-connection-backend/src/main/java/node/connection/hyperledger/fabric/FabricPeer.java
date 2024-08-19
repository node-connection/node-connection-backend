package node.connection.hyperledger.fabric;

import lombok.Builder;
import lombok.Getter;

import java.util.Properties;

@Builder
@Getter
public class FabricPeer {
    private String name;
    private String url;
    private String pemFile;
    private String hostnameOverride;

    Node toNode() {
        Properties properties = new Properties();
        if (hostnameOverride != null) {
            properties.setProperty("ssl-target-name-override", hostnameOverride);
            properties.setProperty("hostnameOverride", hostnameOverride);
        }
        if (pemFile != null) {
            properties.setProperty("pemFile", pemFile);
        }
        return Node.builder()
                .name(name)
                .url(url)
                .properties(properties)
                .build();
    }
}
