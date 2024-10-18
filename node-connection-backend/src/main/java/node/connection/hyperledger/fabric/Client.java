package node.connection.hyperledger.fabric;

import lombok.*;
import node.connection.entity.UserAccount;
import node.connection.hyperledger.fabric.ca.CAEnrollment;
import org.hyperledger.fabric.sdk.User;

import java.util.Set;

@Getter
@Setter
@ToString
public class Client implements User {

    @NonNull
    private String name;
    @NonNull
    private String mspId;
    @NonNull
    private CAEnrollment enrollment;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return null;
    }

    @Override
    public String getAccount() {
        return null;
    }

    @Override
    public String getAffiliation() {
        return null;
    }

    @Override
    public CAEnrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    @Builder
    public Client(@NonNull String name, @NonNull String mspId, @NonNull CAEnrollment enrollment) {
        this.name = name;
        this.mspId = mspId;
        this.enrollment = enrollment;
    }

    public static Client of(UserAccount register, CAEnrollment enrollment) {
        return Client.builder()
                .name(register.getFabricId())
                .mspId(register.getMspId())
                .enrollment(enrollment)
                .build();
    }
}
