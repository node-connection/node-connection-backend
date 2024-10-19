package node.connection._core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.BadRequestException;
import node.connection._core.exception.client.NotFoundException;
import node.connection.entity.UserAccount;
import node.connection.entity.constant.Role;
import node.connection.repository.UserAccountRepository;
import node.connection.service.FabricService;
import org.json.JSONObject;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private final JweDecoder jweDecoder;

    private final UserAccountRepository userAccountRepository;

    private final PasswordEncoder passwordEncoder;


    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   JweDecoder jweDecoder,
                                   UserAccountRepository userAccountRepository,
                                   PasswordEncoder passwordEncoder
    ) {
        super(authenticationManager);
        this.jweDecoder = jweDecoder;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("new request submitted: {}", request.getRequestURI());
        String authContent = request.getHeader("Authorization");

        if(authContent == null) {
            chain.doFilter(request, response);
            return;
        }

        String accessToken = authContent.substring(7);

        String decodedJWT = this.jweDecoder.decode(accessToken);
        JSONObject jsonObject = new JSONObject(decodedJWT);

        String sub = jsonObject.getString("sub");
        String mspId = sub.split(":")[0];
        String number = sub.split(":")[1];
        String secret = sub.split(":")[2];
        String fabricId = FabricService.getId(mspId, number);

        if (request.getRequestURI().contains("/user/register") || request.getRequestURI().contains("/user/login")) {
            Role role = Role.ANONYMOUS;
            setAuthentication(mspId, number, secret, role);
        }
        else {
            UserAccount userAccount = this.userAccountRepository.findByFabricId(fabricId)
                    .orElseThrow(() -> new NotFoundException(ExceptionStatus.USER_NOT_FOUND));

            if (!this.passwordEncoder.matches(secret, userAccount.getSecret())) {
                throw new BadRequestException(ExceptionStatus.INVALID_PASSWORD);
            }

            setAuthentication(userAccount);
        }

        chain.doFilter(request, response);
    }

    private void setAuthentication(UserAccount userAccount) {
        CustomUserDetails userDetails = new CustomUserDetails(userAccount);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setAuthentication(String mspId, String number, String secret, Role role) {
        String id = FabricService.getId(mspId, number);
        UserAccount userAccount = UserAccount.builder().fabricId(id).mspId(mspId).number(number).secret(secret).role(role).build();
        CustomUserDetails userDetails = new CustomUserDetails(userAccount);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
