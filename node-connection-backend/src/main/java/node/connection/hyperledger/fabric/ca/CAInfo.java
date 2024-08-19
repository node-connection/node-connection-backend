package node.connection.hyperledger.fabric.ca;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CAInfo {
    private String name;
    private String url;
    private boolean allowAllHostNames;
    private String pemFile;
}
