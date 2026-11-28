package ec.com.erre.inventory.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityAuditorTest {

    private final SecurityAuditor auditor = new SecurityAuditor();

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void theAuthenticatedUserIsTheAuditor() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                "eromero", null, List.of()));

        assertThat(auditor.getCurrentAuditor()).contains("eromero");
    }

    @Test
    void withoutAnyUserTheAuditorIsTheSystem() {
        assertThat(auditor.getCurrentAuditor()).contains(SecurityAuditor.SYSTEM);
    }

    @Test
    void anAnonymousUserIsNotAnAuditor() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new AnonymousAuthenticationToken(
                                "key",
                                "anonymousUser",
                                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertThat(auditor.getCurrentAuditor()).contains(SecurityAuditor.SYSTEM);
    }
}
