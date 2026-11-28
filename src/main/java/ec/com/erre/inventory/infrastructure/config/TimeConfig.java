package ec.com.erre.inventory.infrastructure.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** One clock for the whole service, in UTC (ADR 0029). Tests can replace it with a fixed one. */
@Configuration
class TimeConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
