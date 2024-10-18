package node.connection._core.config;

import node.connection._core.exception.ExceptionResponseWriter;
import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.ForbiddenException;
import node.connection._core.exception.client.UnauthorizedException;
import node.connection._core.security.CorsConfig;
import node.connection._core.security.JweDecoder;
import node.connection._core.security.JwtAuthenticationFilter;
import node.connection._core.security.JwtExceptionFilter;
import node.connection.repository.UserAccountRepository;
import node.connection.service.FabricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JweDecoder jweDecoder;

    private final UserAccountRepository userAccountRepository;

    private final ExceptionResponseWriter responseWriter;

    private final PasswordEncoder passwordEncoder;


    public SecurityConfig(@Autowired JweDecoder jweDecoder,
                          @Autowired UserAccountRepository userAccountRepository,
                          @Autowired ExceptionResponseWriter responseWriter,
                          @Autowired PasswordEncoder passwordEncoder
    ) {
        this.jweDecoder = jweDecoder;
        this.userAccountRepository = userAccountRepository;
        this.responseWriter = responseWriter;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        PathRequest
                                .toStaticResources()
                                .atCommonLocations()
                )
                .requestMatchers(
                        new AntPathRequestMatcher("/static/css/**"),
                        new AntPathRequestMatcher("/static/scripts/**")
                );
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        http.csrf(CsrfConfigurer::disable);

        http.cors((cors) -> cors.configurationSource(CorsConfig.getConfigurationSource()));

        http.sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.formLogin(FormLoginConfigurer::disable);

        http.httpBasic(HttpBasicConfigurer::disable);

        http.addFilterBefore(
                new JwtAuthenticationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)), jweDecoder, userAccountRepository, passwordEncoder),
                UsernamePasswordAuthenticationFilter.class
        );

        http.addFilterBefore(
                new JwtExceptionFilter(responseWriter),
                JwtAuthenticationFilter.class
        );

        http.exceptionHandling((exceptionHandling) ->
                exceptionHandling.authenticationEntryPoint((request, response, authException) -> {
                    responseWriter.write(response, new UnauthorizedException(ExceptionStatus.UNAUTHORIZED));
                })
        );

        http.exceptionHandling((exceptionHandling) ->
                exceptionHandling.accessDeniedHandler((request, response, accessDeniedException) -> {
                    responseWriter.write(response, new ForbiddenException(ExceptionStatus.FORBIDDEN));
                })
        );

        http.authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                        .requestMatchers(
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/swagger"),
                                new AntPathRequestMatcher("/swagger-ui.html"),
                                new AntPathRequestMatcher("/api-docs/**"),
                                new AntPathRequestMatcher("/v3/api-docs/**")
                        ).permitAll()
                        .anyRequest().authenticated()
        );

        return http.build();
    }
}
