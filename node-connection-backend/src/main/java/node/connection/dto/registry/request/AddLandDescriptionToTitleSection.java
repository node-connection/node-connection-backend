package node.connection.dto.registry.request;

import node.connection.dto.registry.LandDescriptionDto;

public record AddLandDescriptionToTitleSection(
        String documentId,
        LandDescriptionDto landDescription
) {
}
