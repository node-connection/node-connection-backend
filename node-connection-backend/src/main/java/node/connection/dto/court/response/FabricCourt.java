package node.connection.dto.court.response;

import java.util.List;
import java.util.Map;

public record FabricCourt(
        String id,
        String court,
        String support,
        String office,
        String owner,
        List<String> members,
        List<FabricCourtRequest> requests,
        Map<String, FabricCourtRequest> requestsByID,
        Map<String, FabricCourtRequest> finalizedRequestsByID,
        Map<String, FabricCourtRequest> unfinalizedRequestsByID
) {
}
