package node.connection.dto.court.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CourtCreateRequest extends BaseCourtRequest {
    private final String phoneNumber;
    private final String address;
    private final String faxNumber;
    private final List<String> jurisdictions;
    private final String walletPassword;

    @JsonCreator
    public CourtCreateRequest(
            @JsonProperty("court") String court,
            @JsonProperty("support") String support,
            @JsonProperty("office") String office,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("address") String address,
            @JsonProperty("faxNumber") String faxNumber,
            @JsonProperty("jurisdictions") List<String> jurisdictions,
            @JsonProperty("walletPassword") String walletPassword
    ) {
        super(court, support, office);
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.faxNumber = faxNumber;
        this.jurisdictions = jurisdictions;
        this.walletPassword = walletPassword;
    }
}