package node.connection.dto.registry.request;

import node.connection.dto.registry.BuildingPartDescriptionDto;

public record AddBuildingPartDescriptionToExclusivePart(
        String documentId,
        BuildingPartDescriptionDto buildingPartDescription
) {
}
