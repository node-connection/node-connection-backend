package node.connection.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection.entity.pk.IssuanceHistoryKey;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class IssuanceHistory {
    @EmbeddedId
    private IssuanceHistoryKey key;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private UserAccount userAccount;

    @Column
    private String registryDocumentId;

    @Column
    private LocalDateTime expiredAt;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Builder
    public IssuanceHistory(IssuanceHistoryKey key, UserAccount userAccount, String registryDocumentId, LocalDateTime expiredAt) {
        this.key = key;
        this.userAccount = userAccount;
        this.registryDocumentId = registryDocumentId;
        this.expiredAt = expiredAt;
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
}
