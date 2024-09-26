package node.connection.dto.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TitleSectionDto(
        @JsonProperty("buildingDescription") List<BuildingDescriptionDto> buildingDescriptionDto,
        @JsonProperty("landDescription") List<LandDescriptionDto> landDescriptionDto
) {}
