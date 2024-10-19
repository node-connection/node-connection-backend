package node.connection.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection._core.utils.Hash;
import node.connection.dto.root.request.CourtCreateRequest;
import node.connection.entity.pk.CourtKey;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Court {
    @EmbeddedId
    private CourtKey key;

    @Column
    private String channelName;

    @Column
    private String phoneNumber;

    @Column
    private String address;

    @Column
    private String faxNumber;

    @Column(unique = true)
    private String registerCode;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Court(CourtKey key, String channelName, String phoneNumber, String address, String faxNumber, String registerCode) {
        this.key = key;
        this.channelName = channelName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.faxNumber = faxNumber;
        this.registerCode = registerCode;
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

    public static Court of(CourtCreateRequest request) {
        CourtKey courtKey = CourtKey.builder()
                .court(request.court())
                .support(request.support())
                .office(request.office())
                .build();

        String registerCode = Hash.generate();

        return Court.builder()
                .key(courtKey)
                .channelName(request.channelName())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .faxNumber(request.faxNumber())
                .registerCode(registerCode)
                .build();
    }
}