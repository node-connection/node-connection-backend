package node.connection.dto.court.request;

public record FinalizeCourtRequest(
        String requestId,
        String status,
        String errorMessage
) {
}
