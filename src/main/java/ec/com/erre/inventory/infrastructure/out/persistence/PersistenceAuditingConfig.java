package ec.com.erre.inventory.infrastructure.out.persistence;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Data auditing: who comes from the authenticated user ({@code securityAuditor}), when from
 * the UTC clock. On creation only created_* are filled; updated_* stay empty until a change.
 */
@Configuration
@EnableJpaAuditing(
        auditorAwareRef = "securityAuditor",
        dateTimeProviderRef = "auditingTime",
        modifyOnCreate = false)
class PersistenceAuditingConfig {

    @Bean
    DateTimeProvider auditingTime(Clock clock) {
        return () -> Optional.of(Instant.now(clock));
    }
}
