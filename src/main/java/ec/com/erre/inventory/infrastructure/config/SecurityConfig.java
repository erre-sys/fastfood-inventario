package ec.com.erre.inventory.infrastructure.config;

import ec.com.erre.inventory.infrastructure.security.KeycloakRealmRoleConverter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Health is open for Kubernetes; everything else requires a Keycloak token (realm erre), whose
 * realm roles become permissions checked per operation ({@code @PreAuthorize}). Stateless API. CSRF
 * stays on: Spring skips it for requests carrying a bearer token and keeps protecting any request
 * that could rely on cookies. Error dispatches are permitted so an error keeps its original status
 * instead of being turned into a 401 while rendering it.
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(requests ->
                        requests.dispatcherTypeMatchers(DispatcherType.ERROR)
                                .permitAll()
                                .requestMatchers("/actuator/health/**", "/actuator/info")
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .oauth2ResourceServer(server ->
                        server.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        KeycloakRealmRoleConverter.authenticationConverter())))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
