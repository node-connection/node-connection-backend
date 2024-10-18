package node.connection.dto.root.response;

public record FabricCourtRequest(
        String id,
        String documentId,
        String action,
        String payload,
        boolean finalized,
        String requestDate,
        String requestedBy,
        String finalizeDate,
        String finalizedBy,
        String status,
        String errorMessage,
        String forwardedTo,
        String forwardedFrom
) {
}
