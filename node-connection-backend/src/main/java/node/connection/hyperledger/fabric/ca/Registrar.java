package node.connection.hyperledger.fabric.ca;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hyperledger.fabric.sdk.User;

import java.util.Objects;
import java.util.Set;

@Builder
@Getter
@Setter
public class Registrar implements User {
    private String name;
    private CAEnrollment enrollment;

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
    public String getMspId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registrar registrar)) return false;
        return Objects.equals(name, registrar.name) &&
                Objects.equals(enrollment, registrar.enrollment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, enrollment);
    }
}
