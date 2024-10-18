package node.connection.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LandDescription {
    @JsonProperty("displayNumber")
    private String displayNumber;

    @JsonProperty("locationNumber")
    private String locationNumber;

    @JsonProperty("landType")
    private String landType;

    @JsonProperty("area")
    private String area;

    @JsonProperty("registrationCause")
    private String registrationCause;
}
