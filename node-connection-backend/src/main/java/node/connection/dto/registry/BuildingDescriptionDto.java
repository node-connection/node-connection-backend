package node.connection.dto.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BuildingDescriptionDto(
        @JsonProperty("displayNumber") String displayNumber,
        @JsonProperty("receiptDate") String receiptDate,
        @JsonProperty("locationNumber") String locationNumber,
        @JsonProperty("buildingDetails") String buildingDetails,
        @JsonProperty("registrationCause") String registrationCause
) {}
