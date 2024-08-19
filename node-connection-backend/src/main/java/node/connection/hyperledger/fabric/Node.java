package node.connection.hyperledger.fabric;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Properties;

@Builder
@Getter
@Setter
public class Node {
    private String name;
    private String url;
    private Properties properties;
}
