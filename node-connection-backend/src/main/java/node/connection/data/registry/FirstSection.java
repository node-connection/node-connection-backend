package node.connection.data.registry;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record FirstSection(
        @JsonProperty("rankNumber") int rankNumber,
        @JsonProperty("registrationPurpose") String registrationPurpose,
        @JsonProperty("receiptDate") String receiptDate, // Use String or a suitable date format
        @JsonProperty("registrationCause") String registrationCause,
        @JsonProperty("holderAndAdditionalInfo") String holderAndAdditionalInfo
) {}

