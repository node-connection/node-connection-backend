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
public class JurisdictionKey implements Serializable {
    private String city;
    private String district;

    @Builder
    public JurisdictionKey(String city, String district) {
        this.city = city;
        this.district = district;
    }
}
