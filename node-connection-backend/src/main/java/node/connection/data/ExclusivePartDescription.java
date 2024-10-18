package node.connection.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExclusivePartDescription {
    @JsonProperty("buildingPartDescription")
    private List<BuildingPartDescription> buildingPartDescription;

    @JsonProperty("landRightDescription")
    private List<LandRightDescription> landRightDescription;
}
