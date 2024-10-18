package node.connection.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TitleSection {
    @JsonProperty("buildingDescription")
    private List<BuildingDescription> buildingDescription;

    @JsonProperty("landDescription")
    private List<LandDescription> landDescription;
}
