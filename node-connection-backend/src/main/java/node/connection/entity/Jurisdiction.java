package node.connection.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection.dto.root.request.CourtCreateRequest;
import node.connection.entity.pk.CourtKey;
import node.connection.entity.pk.JurisdictionKey;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Jurisdiction {
    @Id
    private JurisdictionKey key;

    @ManyToOne(cascade = CascadeType.ALL)
    private Court court;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Jurisdiction(JurisdictionKey key, Court court) {
        this.key = key;
        this.court = court;
    }

    @PrePersist
    protected void onCreated() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected  void onUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Jurisdiction of(String city, String district, Court court) {
        JurisdictionKey newKey = JurisdictionKey.builder()
                .city(city)
                .district(district)
                .build();

        return new Jurisdiction(newKey, court);
    }
}
