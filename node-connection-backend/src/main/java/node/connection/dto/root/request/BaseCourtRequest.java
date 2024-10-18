package node.connection.dto.root.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BaseCourtRequest {
    private final String court;
    private final String support;
    private final String office;

    @Builder
    public BaseCourtRequest(String court, String support, String office) {
        this.court = court;
        this.support = support;
        this.office = office;
    }
}
