package ec.com.erre.fastfood.inventory;

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

/** Checks which paths are public and which require a token, with the service really running. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SecurityTest {

    @Value("${local.server.port}")
    int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private int status(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
        return client.send(request, BodyHandlers.discarding()).statusCode();
    }

    @Test
    void healthIsPublicForKubernetes() throws Exception {
        assertThat(status("/actuator/health/liveness")).isEqualTo(200);
        assertThat(status("/actuator/health/readiness")).isEqualTo(200);
    }

    @Test
    void everyOtherPathRequiresAToken() throws Exception {
        assertThat(status("/api/units")).isEqualTo(401);
    }
}
