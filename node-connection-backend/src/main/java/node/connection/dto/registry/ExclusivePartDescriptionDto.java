package node.connection.dto.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ExclusivePartDescriptionDto(
        @JsonProperty("buildingPartDescription") List<BuildingPartDescriptionDto> buildingPartDescriptionDto,
        @JsonProperty("landRightDescription") List<LandRightDescriptionDto> landRightDescriptionDto
) {}
