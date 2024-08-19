package node.connection.data.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record BuildingDescription(
        @JsonProperty("displayNumber") String displayNumber,
        @JsonProperty("receiptDate") String receiptDate, // Use String or a suitable date format
        @JsonProperty("locationNumber") String locationNumber,
        @JsonProperty("buildingDetails") String buildingDetails,
        @JsonProperty("registrationCause") String registrationCause,
        @JsonProperty("locationNumberAlternate") String locationNumberAlternate
) {}
