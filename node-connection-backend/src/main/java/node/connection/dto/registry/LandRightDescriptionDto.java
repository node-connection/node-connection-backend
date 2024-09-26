package node.connection.dto.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LandRightDescriptionDto(
        @JsonProperty("displayNumber") String displayNumber,
        @JsonProperty("landRightType") String landRightType,
        @JsonProperty("landRightRatio") String landRightRatio,
        @JsonProperty("registrationCause") String registrationCause
) {}
