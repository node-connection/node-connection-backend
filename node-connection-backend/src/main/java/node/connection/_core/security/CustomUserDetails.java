package node.connection._core.security;

import node.connection.entity.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public record CustomUserDetails(UserAccount userAccount) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(userAccount.getRole().getRoleName()));
    }

    @Override
    public String getPassword() {
        return userAccount.getSecret();
    }

    @Override
    public String getUsername() {
        return userAccount.getFabricId();
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }
}
