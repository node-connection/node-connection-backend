package node.connection._core.security;

import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.NotFoundException;
import node.connection.entity.UserAccount;
import node.connection.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;


    public CustomUserDetailsService(@Autowired UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByFabricId(name)
                .orElseThrow(() -> new NotFoundException(ExceptionStatus.USER_NOT_FOUND));

        return new CustomUserDetails(userAccount);
    }
}
