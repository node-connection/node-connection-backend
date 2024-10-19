package node.connection.entity.pk;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class IssuanceHistoryKey implements Serializable {
    private String address;
    private String detailAddress;
    private String issuanceHash;

    @Builder
    public IssuanceHistoryKey(String address, String detailAddress, String issuanceHash) {
        this.address = address;
        this.detailAddress = detailAddress;
        this.issuanceHash = issuanceHash;
    }
}
