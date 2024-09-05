package node.connection.dto.court.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DeleteCourtMemberRequest extends BaseCourtRequest {
    private final String memberId;

    @JsonCreator
    public DeleteCourtMemberRequest(
            @JsonProperty("court") String court,
            @JsonProperty("support") String support,
            @JsonProperty("office") String office,
            @JsonProperty("memberId") String memberId
    ) {
        super(court, support, office);
        this.memberId = memberId;
    }
}
