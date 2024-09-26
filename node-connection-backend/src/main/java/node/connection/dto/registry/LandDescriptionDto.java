package node.connection.dto.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LandDescriptionDto(
        @JsonProperty("displayNumber") String displayNumber,
        @JsonProperty("locationNumber") String locationNumber,
        @JsonProperty("landType") String landType,
        @JsonProperty("area") String area,
        @JsonProperty("registrationCause") String registrationCause
) {}
