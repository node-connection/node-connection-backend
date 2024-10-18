package node.connection._core.utils;

import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.ForbiddenException;
import node.connection._core.security.CustomUserDetails;
import node.connection.entity.constant.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class AccessControl {

    public void hasAdminRole(CustomUserDetails userDetails) {
        boolean granted = this.isRoleGranted(userDetails, Role.ADMIN);
        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    public void hasRootRole(CustomUserDetails userDetails) {
        boolean granted = this.isRoleGranted(userDetails, Role.ROOT);
        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    public void hasRegistryRole(CustomUserDetails userDetails) {
        boolean granted = this.isRoleGranted(userDetails, Role.REGISTRY);
        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    public void hasViewerRole(CustomUserDetails userDetails) {
        boolean granted = this.isRoleGranted(userDetails, Role.VIEWER);
        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    public void hasMemberRole(CustomUserDetails userDetails) {
        boolean REGISTRY_GRANTED = this.isRoleGranted(userDetails, Role.REGISTRY);
        boolean VIEWER_GRANTED = this.isRoleGranted(userDetails, Role.VIEWER);

        if (!REGISTRY_GRANTED && !VIEWER_GRANTED) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    public void hasAnonymousRole(CustomUserDetails userDetails) {
        boolean granted = this.isRoleGranted(userDetails, Role.ANONYMOUS);
        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    private boolean isRoleGranted(CustomUserDetails userDetails, Role role) {
        return userDetails.getAuthorities()
                .stream()
                .anyMatch(authority -> hasRole(authority, role));
    }

    private boolean hasRole(GrantedAuthority authorities, Role role) {
        return authorities.toString().equals(role.getRoleName()) || authorities.toString().equals(Role.ROOT.getRoleName());
    }
}
