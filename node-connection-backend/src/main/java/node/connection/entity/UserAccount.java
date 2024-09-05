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
@Getter
@NoArgsConstructor
public class UserAccount {
    @Id
    private String name;

    @Column
    private String mspId;

    @Column(unique = true)
    private String number;

    @Column
    private String secret;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String enrollment;

    @Column(length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Builder
    public UserAccount(String name, String mspId, String number, String secret, String enrollment, Role role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name = name;
        this.mspId = mspId;
        this.number = number;
        this.secret = secret;
        this.enrollment = enrollment;
        this.role = role;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static UserAccount of(String name, String mspId, String secret, String enrollment) {
        return UserAccount.builder()
                .name(name)
                .mspId(mspId)
                .secret(secret)
                .enrollment(enrollment)
                .role(getRoleByMspId(mspId))
                .build();
    }

    public static UserAccount of(Registrar registrar, String secret, String enrollment) {
        return UserAccount.builder()
                .name(registrar.getName())
                .mspId(registrar.getMspId())
                .secret(secret)
                .enrollment(enrollment)
                .role(Role.ADMIN)
                .build();
    }

    public static UserAccount of(Client client, String number, String secret, String enrollment) {
        return UserAccount.builder()
                .name(client.getName())
                .mspId(client.getMspId())
                .number(number)
                .secret(secret)
                .enrollment(enrollment)
                .role(getRoleByMspId(client.getMspId()))
                .build();
    }

    public static UserAccount of(Client client, String number, String secret, String enrollment, Role role) {
        return UserAccount.builder()
                .name(client.getName())
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
}
