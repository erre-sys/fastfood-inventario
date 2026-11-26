package ec.com.erre.fastfood.inventario;

import static org.assertj.core.api.Assertions.assertThat;

import ec.com.erre.fastfood.inventario.dominio.Dimension;
import ec.com.erre.fastfood.inventario.dominio.Unidad;
import ec.com.erre.fastfood.inventario.dominio.puerto.UnidadRepositorio;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/** La migración y el adaptador JPA, contra un PostgreSQL real (Testcontainers). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class UnidadRepositorioTest {

    @Autowired UnidadRepositorio repositorio;

    @Test
    void laMigracionCargaLasDoceUnidadesEstandar() {
        assertThat(repositorio.listar()).hasSize(12);
    }

    @Test
    void encuentraUnaUnidadPorSuCodigo() {
        var kilo = repositorio.buscarPorCodigo("kg").orElseThrow();

        assertThat(kilo.dimension()).isEqualTo(Dimension.MASA);
        assertThat(kilo.factorABase()).isEqualByComparingTo("1000");
    }

    @Test
    void guardaUnaUnidadPropiaDelNegocio() {
        var porcion =
                new Unidad("porcion180", "Porción de 180 g", Dimension.MASA, new BigDecimal("180"));

        repositorio.guardar(porcion);

        assertThat(repositorio.existe("porcion180")).isTrue();
        assertThat(repositorio.buscarPorCodigo("porcion180").orElseThrow().nombre())
                .isEqualTo("Porción de 180 g");
    }
}
