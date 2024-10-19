package node.connection.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection.entity.pk.AddressIndexKey;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class RegistryDocumentIndex {
    @EmbeddedId
    private AddressIndexKey key;

    @Column
    private String documentId;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Builder
    public RegistryDocumentIndex(AddressIndexKey key, String documentId) {
        this.key = key;
        this.documentId = documentId;
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
