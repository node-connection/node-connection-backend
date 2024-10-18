package node.connection.dto.root.request;

import java.util.List;

public record CourtCreateRequest(
        String court,
        String support,
        String office,
        String channelName,
        String phoneNumber,
        String address,
        String faxNumber,
        String city,
        List<String> districts
) {
}