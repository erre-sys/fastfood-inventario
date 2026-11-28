package ec.com.erre.inventory.infrastructure.security;

import static ec.com.erre.inventory.infrastructure.security.Permissions.ITEMS_CREATE;
import static ec.com.erre.inventory.infrastructure.security.Permissions.ITEMS_EDIT;
import static ec.com.erre.inventory.infrastructure.security.Permissions.ITEMS_READ;
import static ec.com.erre.inventory.infrastructure.security.Permissions.UNITS_CREATE;
import static ec.com.erre.inventory.infrastructure.security.Permissions.UNITS_EDIT;
import static ec.com.erre.inventory.infrastructure.security.Permissions.UNITS_READ;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    private static Jwt token(Map<String, Object> claims) {
        var builder =
                Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .subject("8f2c-user-id")
                        .claim("preferred_username", "eromero");
        claims.forEach(builder::claim);
        return builder.build();
    }

    private static Jwt tokenWithRoles(String... roles) {
        return token(Map.of("realm_access", Map.of("roles", List.of(roles))));
    }

    private Set<String> authorities(Jwt jwt) {
        return converter.convert(jwt).stream().map(GrantedAuthority::getAuthority).collect(toSet());
    }

    @Test
    void adminGetsEveryUnitsPermission() {
        assertThat(authorities(tokenWithRoles("ADMIN")))
                .containsExactlyInAnyOrder(
                        "ROLE_ADMIN",
                        UNITS_READ,
                        UNITS_CREATE,
                        UNITS_EDIT,
                        ITEMS_READ,
                        ITEMS_CREATE,
                        ITEMS_EDIT);
    }

    @Test
    void cashierAndSellerCanOnlyRead() {
        assertThat(authorities(tokenWithRoles("CAJERO")))
                .containsExactlyInAnyOrder("ROLE_CAJERO", UNITS_READ, ITEMS_READ);
        assertThat(authorities(tokenWithRoles("VENDEDOR")))
                .containsExactlyInAnyOrder("ROLE_VENDEDOR", UNITS_READ, ITEMS_READ);
    }

    @Test
    void permissionsOfSeveralRolesAreCombined() {
        assertThat(authorities(tokenWithRoles("CAJERO", "ADMIN")))
                .containsExactlyInAnyOrder(
                        "ROLE_CAJERO",
                        "ROLE_ADMIN",
                        UNITS_READ,
                        UNITS_CREATE,
                        UNITS_EDIT,
                        ITEMS_READ,
                        ITEMS_CREATE,
                        ITEMS_EDIT);
    }

    @Test
    void keycloakDefaultRolesGrantNoPermission() {
        assertThat(authorities(tokenWithRoles("default-roles-erre", "offline_access")))
                .containsExactlyInAnyOrder("ROLE_default-roles-erre", "ROLE_offline_access");
    }

    @Test
    void roleNamesAreExact() {
        assertThat(authorities(tokenWithRoles("admin"))).containsExactly("ROLE_admin");
    }

    @Test
    void aTokenWithoutRealmRolesGrantsNothing() {
        assertThat(authorities(token(Map.of()))).isEmpty();
    }

    @Test
    void theUsernameIsThePrincipal() {
        var authentication =
                KeycloakRealmRoleConverter.authenticationConverter()
                        .convert(tokenWithRoles("CAJERO"));

        assertThat(authentication.getName()).isEqualTo("eromero");
    }
}
