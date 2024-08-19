package node.connection.hyperledger.fabric;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

@Builder
@Getter
@Setter
@ToString
public class FabricProposalResponse {
    private Boolean success;
    private String message;
    private String transactionID;
    private String payload;

    public static FabricProposalResponse of(ProposalResponse proposalResponse) {
        boolean success = proposalResponse.getStatus() == ProposalResponse.Status.SUCCESS;
        String payload = null;
        if (success) {
            try {
                payload = new String(proposalResponse.getChaincodeActionResponsePayload());
            } catch (InvalidArgumentException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return FabricProposalResponse.builder()
                .success(success)
                .message(proposalResponse.getMessage())
                .transactionID(proposalResponse.getTransactionID())
                .payload(payload)
                .build();
    }

    public static FabricProposalResponse cannotUsableService() {
        return FabricProposalResponse.builder()
                .success(false)
                .message("KapanetFabricService 초기화가 필요합니다.")
                .build();
    }
}
