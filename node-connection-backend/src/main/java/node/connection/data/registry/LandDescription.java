package node.connection.data.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LandDescription(
        @JsonProperty("displayNumber") String displayNumber,
        @JsonProperty("landType") String landType,
        @JsonProperty("area") double area,
        @JsonProperty("registrationCause") String registrationCause
) {}
