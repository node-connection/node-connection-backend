package node.connection.hyperledger.fabric;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperledger.fabric.sdk.TransactionInfo;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
public class FabricTransactionInfo {
    private FabricHeader header;
    private List<FabricPayload> payloads;


    public static FabricTransactionInfo of(TransactionInfo info) {
        // Header
        FabricHeader header = FabricHeader.of(info);
        // Payload
        List<FabricPayload> payloads = FabricPayload.parseFrom(info);
        return FabricTransactionInfo.builder()
                .header(header)
                .payloads(payloads)
                .build();
    }

}
