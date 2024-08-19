package node.connection.hyperledger.fabric.ca;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CAUser {
    private String name;
    private String secret;
}
