package ec.com.erre.inventory.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves the contract file for Swagger UI, only when Swagger UI is enabled (dev profile). The API
 * is documented by the contract, never generated from the code.
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true")
class ApiDocsConfig implements WebMvcConfigurer {

    static final String CONTRACT_PATH = "/openapi/**";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(CONTRACT_PATH).addResourceLocations("classpath:/openapi/");
    }
}
