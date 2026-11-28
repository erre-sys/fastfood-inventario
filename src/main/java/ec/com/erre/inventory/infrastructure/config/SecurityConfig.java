package ec.com.erre.inventory.infrastructure.config;

import ec.com.erre.inventory.infrastructure.security.KeycloakRealmRoleConverter;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
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
 * instead of being turned into a 401 while rendering it. Swagger UI and the contract file are open
 * only when Swagger UI is enabled (dev profile).
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig {

    private static final String[] API_DOCS = {
        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", ApiDocsConfig.CONTRACT_PATH
    };

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${springdoc.swagger-ui.enabled:false}") boolean apiDocsEnabled) {
        if (apiDocsEnabled) {
            http.authorizeHttpRequests(requests -> requests.requestMatchers(API_DOCS).permitAll());
        }
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
