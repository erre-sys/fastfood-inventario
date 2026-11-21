package ec.com.erre.fastfood.inventario.infraestructura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Salud abierta para Kubernetes; el resto de rutas exige un token de Keycloak (realm erre). API sin
 * sesiones: cada petición trae su token en la cabecera Authorization, así que no aplica CSRF.
 */
@Configuration
class SeguridadConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http) {
        http.authorizeHttpRequests(peticiones ->
                        peticiones
                                .requestMatchers("/actuator/health/**", "/actuator/info")
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .oauth2ResourceServer(servidor -> servidor.jwt(Customizer.withDefaults()))
                .sessionManagement(
                        sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
