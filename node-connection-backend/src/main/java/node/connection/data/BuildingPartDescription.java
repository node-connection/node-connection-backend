package node.connection.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BuildingPartDescription {
    @JsonProperty("displayNumber")
    private String displayNumber;

    @JsonProperty("receiptDate")
    private String receiptDate;

    @JsonProperty("partNumber")
    private String partNumber;

    @JsonProperty("buildingDetails")
    private String buildingDetails;

    @JsonProperty("registrationCause")
    private String registrationCause;
}
