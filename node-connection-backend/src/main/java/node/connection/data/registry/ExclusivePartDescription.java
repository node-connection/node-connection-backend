package node.connection.data.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ExclusivePartDescription(
        @JsonProperty("buildingDescription") List<BuildingDescription> buildingDescription,
        @JsonProperty("landRightDescription") List<LandRightDescription> landRightDescription
) {}
