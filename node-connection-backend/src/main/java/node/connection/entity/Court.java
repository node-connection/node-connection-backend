package node.connection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection.dto.court.request.CourtCreateRequest;
import node.connection.entity.pk.CourtKey;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Court {
    @EmbeddedId
    private CourtKey key;

    @Column
    private String phoneNumber;

    @Column
    private String address;

    @Column
    private String faxNumber;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Court(CourtKey key, String phoneNumber, String address, String faxNumber, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.key = key;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.faxNumber = faxNumber;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static Court of(String court, String support, String office, String phoneNumber, String address, String faxNumber) {
        CourtKey courtKey = CourtKey.builder()
                .court(court)
                .support(support)
                .office(office)
                .build();

        return Court.builder()
                .key(courtKey)
                .phoneNumber(phoneNumber)
                .address(address)
                .faxNumber(faxNumber)
                .build();
    }

    public static Court of(CourtCreateRequest request) {
        CourtKey courtKey = CourtKey.builder()
                .court(request.getCourt())
                .support(request.getSupport())
                .office(request.getOffice())
                .build();

        return Court.builder()
                .key(courtKey)
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .faxNumber(request.getFaxNumber())
                .build();
    }
}
