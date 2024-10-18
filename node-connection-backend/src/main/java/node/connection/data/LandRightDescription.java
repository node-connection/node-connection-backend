package node.connection.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LandRightDescription {
    @JsonProperty("displayNumber")
    private String displayNumber;

    @JsonProperty("landRightType")
    private String landRightType;

    @JsonProperty("landRightRatio")
    private String landRightRatio;

    @JsonProperty("registrationCause")
    private String registrationCause;
}
