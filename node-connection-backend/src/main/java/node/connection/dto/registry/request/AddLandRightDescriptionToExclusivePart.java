package node.connection.dto.registry.request;

import node.connection.dto.registry.LandRightDescriptionDto;

public record AddLandRightDescriptionToExclusivePart(
        String documentId,
        LandRightDescriptionDto landRightDescription
) {
}
