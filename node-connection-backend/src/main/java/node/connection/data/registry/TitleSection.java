package node.connection.data.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TitleSection(
        @JsonProperty("buildingDescription") List<BuildingDescription> buildingDescription,
        @JsonProperty("landDescription") List<LandDescription> landDescription
) {}
