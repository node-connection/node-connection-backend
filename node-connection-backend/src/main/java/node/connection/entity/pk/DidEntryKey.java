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
public class DidEntryKey implements Serializable {
    private String phoneNumber;
    private String did;

    @Builder
    public DidEntryKey(String phoneNumber, String did) {
        this.phoneNumber = phoneNumber;
        this.did = did;
    }
}
