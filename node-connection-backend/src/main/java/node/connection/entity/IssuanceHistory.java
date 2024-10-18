package node.connection.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class IssuanceHistory {
    @Id
    private String issuanceHash;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private UserAccount userAccount;

    @Column
    private String registryDocumentId;

    @Column
    private String address;

    @Column
    private String detailAddress;

    @Column
    private LocalDateTime expiredAt;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Builder
    public IssuanceHistory(String issuanceHash, UserAccount userAccount, String registryDocumentId, String address, String detailAddress, LocalDateTime expiredAt) {
        this.issuanceHash = issuanceHash;
        this.userAccount = userAccount;
        this.registryDocumentId = registryDocumentId;
        this.address = address;
        this.detailAddress = detailAddress;
        this.expiredAt = expiredAt;
    }

    @PrePersist
    protected void onCreated() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected  void onUpdated() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
