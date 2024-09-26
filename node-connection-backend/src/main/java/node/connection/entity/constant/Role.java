package node.connection.entity.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    ANONYMOUS("ROLE_ANONYMOUS"),
    VIEWER("ROLE_VIEWER"),
    REGISTRY("ROLE_REGISTRY"),
    ROOT("ROLE_ROOT"),
    ADMIN("ROLE_ADMIN");

    @Getter
    private final String roleName;
}

