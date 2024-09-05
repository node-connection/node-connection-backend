package node.connection.data.court;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CourtRequest {
    @JsonProperty("id")
    private String id;

    @JsonProperty("documentId")
    private String documentId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("payload")
    private String payload;

    @JsonProperty("finalized")
    private boolean finalized = false;

    @JsonProperty("requestDate")
    private String requestDate;

    @JsonProperty("requestedBy")
    private String requestedBy = "";

    @JsonProperty("finalizeDate")
    private String finalizeDate = "-";

    @JsonProperty("finalizedBy")
    private String finalizedBy = "";

    @JsonProperty("status")
    private String status = "Pending";

    @JsonProperty("errorMessage")
    private String errorMessage = "-";

    @JsonProperty("forwardedTo")
    private String forwardedTo = "-";

    @JsonProperty("forwardedFrom")
    private String forwardedFrom = "-";

    @Builder
    public CourtRequest(String id, String documentId, String action, String payload, String requestDate) {
        this.id = id;
        this.documentId = documentId;
        this.action = action;
        this.payload = payload;
        this.requestDate = requestDate == null ? LocalDateTime.now().toString() : requestDate;
    }
}
