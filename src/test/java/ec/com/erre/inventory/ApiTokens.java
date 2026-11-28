package ec.com.erre.inventory;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import ec.com.erre.inventory.infrastructure.security.KeycloakRealmRoleConverter;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * A request authenticated as a Keycloak user with one realm role, converted by the same converter
 * as production: roles, permissions and username come out exactly as with a real token.
 */
final class ApiTokens {

    private ApiTokens() {}

    static RequestPostProcessor as(String username, String role) {
        var jwt =
                Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .subject("id-" + username)
                        .claim("preferred_username", username)
                        .claim("realm_access", Map.of("roles", List.of(role)))
                        .build();
        return authentication(KeycloakRealmRoleConverter.authenticationConverter().convert(jwt));
    }

    static RequestPostProcessor admin() {
        return as("eromero", "ADMIN");
    }

    static RequestPostProcessor cashier() {
        return as("cajero1", "CAJERO");
    }

    static RequestPostProcessor seller() {
        return as("vendedor1", "VENDEDOR");
    }
}
