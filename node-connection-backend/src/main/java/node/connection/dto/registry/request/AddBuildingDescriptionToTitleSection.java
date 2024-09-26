package node.connection.dto.registry.request;

import node.connection.dto.registry.BuildingDescriptionDto;

public record AddBuildingDescriptionToTitleSection(
        String documentId,
        BuildingDescriptionDto buildingDescription
) {
}
