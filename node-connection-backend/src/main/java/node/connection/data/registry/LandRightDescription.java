package node.connection.data.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LandRightDescription(
        @JsonProperty("displayNumber") String displayNumber,
        @JsonProperty("landRightType") String landRightType,
        @JsonProperty("landRightRatio") double landRightRatio,
        @JsonProperty("registrationCause") String registrationCause
) {}
