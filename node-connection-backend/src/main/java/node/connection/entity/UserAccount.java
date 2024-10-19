package node.connection.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import node.connection.entity.constant.Role;
import node.connection.hyperledger.fabric.Client;
import node.connection.hyperledger.fabric.ca.Registrar;
import node.connection.service.FabricService;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "user_account_tb")
@Table(
        name = "user_account_tb",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"mpsId", "number"})
        }
)
@Getter
@NoArgsConstructor
public class UserAccount {
    @Id
    private String fabricId;

    @Column
    private String mspId;

    @Column
    private String number;

    @Column
    private String secret;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String enrollment;

    @Column(length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private String userName;

    @Column
    private String phoneNumber;

    @Column
    private String email;

    @ManyToOne(cascade = CascadeType.REFRESH)
    private Court court;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;


    @Builder
    public UserAccount(String fabricId, String mspId, String number, String secret, String enrollment, Role role, String userName, String phoneNumber, String email, Court court, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.fabricId = fabricId;
        this.mspId = mspId;
        this.number = number;
        this.secret = secret;
        this.enrollment = enrollment;
        this.role = role;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.email = email;
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

    public static UserAccount of(String fabricId, String mspId, String secret, String enrollment) {
        return UserAccount.builder()
                .fabricId(fabricId)
                .mspId(mspId)
                .secret(secret)
                .enrollment(enrollment)
                .role(getRoleByMspId(mspId))
                .build();
    }

    public static UserAccount of(Registrar registrar, String secret, String enrollment) {
        return UserAccount.builder()
                .fabricId(registrar.getName())
                .mspId(registrar.getMspId())
                .secret(secret)
                .enrollment(enrollment)
                .role(Role.ADMIN)
                .build();
    }

    public static UserAccount of(Client client, String number, String secret, String enrollment) {
        return UserAccount.builder()
                .fabricId(client.getName())
                .mspId(client.getMspId())
                .number(number)
                .secret(secret)
                .enrollment(enrollment)
                .role(getRoleByMspId(client.getMspId()))
                .build();
    }

    public static UserAccount of(Client client, String number, String secret, String enrollment, Role role) {
        return UserAccount.builder()
                .fabricId(client.getName())
                .mspId(client.getMspId())
                .number(number)
                .secret(secret)
                .enrollment(enrollment)
                .role(role)
                .build();
    }

    public static Role getRoleByMspId(String mspId) {
        if (Objects.equals(mspId, FabricService.VIEWER_MSP)) {
            return Role.VIEWER;
        }
        else if (Objects.equals(mspId, FabricService.REGISTRY_MSP)) {
            return Role.REGISTRY;
        }
        else {
            return Role.ANONYMOUS;
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCourt(Court court) {
        this.court = court;
    }

    public void setEnrollment(String enrollment) {
        this.enrollment = enrollment;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
