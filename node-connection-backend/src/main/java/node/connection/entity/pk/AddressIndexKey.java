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
public class AddressIndexKey implements Serializable {
    private String address;
    private String detailAddress;

    @Builder
    public AddressIndexKey(String address, String detailAddress) {
        this.address = address;
        this.detailAddress = detailAddress;
    }
}
