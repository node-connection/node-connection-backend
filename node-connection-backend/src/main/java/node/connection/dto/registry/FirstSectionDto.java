package node.connection.dto.registry;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FirstSectionDto(
        @JsonProperty("rankNumber") String rankNumber,
        @JsonProperty("registrationPurpose") String registrationPurpose,
        @JsonProperty("receiptDate") String receiptDate, // Use String or a suitable date format
        @JsonProperty("registrationCause") String registrationCause,
        @JsonProperty("holderAndAdditionalInfo") String holderAndAdditionalInfo
) {}

