package node.connection.dto.court.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CourtRequest {
    private final String court;
    private final String support;
    private final String office;

    @Builder
    public CourtRequest(String court, String support, String office) {
        this.court = court;
        this.support = support;
        this.office = office;
    }

    public String getCourtId() {
        return court + "_" + support + "_" + office;
    }
}
