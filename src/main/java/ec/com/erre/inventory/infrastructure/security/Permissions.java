package ec.com.erre.inventory.infrastructure.security;

import java.util.Map;
import java.util.Set;

/**
 * Permissions checked by the API, and the realm roles (Keycloak, realm erre) that grant them. The
 * mapping lives in the service, so changing what a role can do is a reviewed code change.
 */
public final class Permissions {

    public static final String UNITS_READ = "units:read";
    public static final String UNITS_CREATE = "units:create";
    public static final String UNITS_EDIT = "units:edit";

    private static final Map<String, Set<String>> BY_ROLE =
            Map.of(
                    "ADMIN", Set.of(UNITS_READ, UNITS_CREATE, UNITS_EDIT),
                    "CAJERO", Set.of(UNITS_READ),
                    "VENDEDOR", Set.of(UNITS_READ));

    private Permissions() {}

    /** Permissions granted by a realm role; none for an unknown role. Role names are exact. */
    public static Set<String> grantedBy(String role) {
        return BY_ROLE.getOrDefault(role, Set.of());
    }
}
