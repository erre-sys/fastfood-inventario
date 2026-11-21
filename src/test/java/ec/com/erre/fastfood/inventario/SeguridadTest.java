package ec.com.erre.fastfood.inventario;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

/** Verifica qué rutas son públicas y cuáles exigen token, con el servicio corriendo de verdad. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SeguridadTest {

    @Value("${local.server.port}")
    int puerto;

    private final HttpClient cliente = HttpClient.newHttpClient();

    private int estado(String ruta) throws Exception {
        var peticion =
                HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta)).build();
        return cliente.send(peticion, BodyHandlers.discarding()).statusCode();
    }

    @Test
    void laSaludEsPublicaParaKubernetes() throws Exception {
        assertThat(estado("/actuator/health/liveness")).isEqualTo(200);
        assertThat(estado("/actuator/health/readiness")).isEqualTo(200);
    }

    @Test
    void lasDemasRutasExigenToken() throws Exception {
        assertThat(estado("/api/insumos")).isEqualTo(401);
    }
}
