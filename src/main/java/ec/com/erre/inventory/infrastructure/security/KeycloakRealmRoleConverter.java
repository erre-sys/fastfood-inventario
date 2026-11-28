package ec.com.erre.inventory.infrastructure.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Turns the realm roles of a Keycloak token ({@code realm_access.roles}) into Spring authorities:
 * {@code ROLE_<role>} plus the permissions that role grants. Unknown roles, such as Keycloak's
 * {@code default-roles-erre}, grant no permission.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    static final String REALM_ACCESS = "realm_access";
    static final String ROLES = "roles";
    static final String USERNAME = "preferred_username";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);
        if (realmAccess == null || !(realmAccess.get(ROLES) instanceof Collection<?> roles)) {
            return List.of();
        }
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (Object role : roles) {
            if (role instanceof String name) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + name));
                Permissions.grantedBy(name)
                        .forEach(permission ->
                                authorities.add(new SimpleGrantedAuthority(permission)));
            }
        }
        return authorities;
    }

    /**
     * Authentication for Keycloak tokens: realm roles as authorities, the username as principal.
     */
    public static JwtAuthenticationConverter authenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        converter.setPrincipalClaimName(USERNAME);
        return converter;
    }
}
