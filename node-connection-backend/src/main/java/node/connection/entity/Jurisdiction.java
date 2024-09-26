package node.connection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection.entity.pk.CourtKey;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Jurisdiction {
    @Id
    private String jurisdiction;

    @ManyToOne
    private Court court;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Jurisdiction(String jurisdiction, Court court, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.jurisdiction = jurisdiction;
        this.court = court;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static Jurisdiction of(String jurisdiction, Court court) {
        return Jurisdiction.builder()
                .jurisdiction(jurisdiction)
                .court(court)
                .build();
    }
}
