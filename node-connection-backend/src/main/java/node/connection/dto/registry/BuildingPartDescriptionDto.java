package node.connection.dto.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BuildingPartDescriptionDto(
        @JsonProperty("displayNumber") String displayNumber,
        @JsonProperty("receiptDate") String receiptDate, // Use String or a suitable date format
        @JsonProperty("partNumber") String partNumber,
        @JsonProperty("buildingDetails") String buildingDetails,
        @JsonProperty("registrationCause") String registrationCause
) {}
