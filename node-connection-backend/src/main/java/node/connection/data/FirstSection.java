package node.connection.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FirstSection {
    @JsonProperty("rankNumber")
    private String rankNumber;

    @JsonProperty("registrationPurpose")
    private String registrationPurpose;

    @JsonProperty("receiptDate")
    private String receiptDate;

    @JsonProperty("registrationCause")
    private String registrationCause;

    @JsonProperty("holderAndAdditionalInfo")
    private String holderAndAdditionalInfo;
}
