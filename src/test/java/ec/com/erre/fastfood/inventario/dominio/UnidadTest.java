package ec.com.erre.fastfood.inventario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UnidadTest {

    @Test
    void unaUnidadConFactorUnoEsLaBaseDeSuDimension() {
        var gramo = new Unidad("g", "Gramo", Dimension.MASA, new BigDecimal("1"));
        var kilo = new Unidad("kg", "Kilogramo", Dimension.MASA, new BigDecimal("1000"));

        assertThat(gramo.esBase()).isTrue();
        assertThat(kilo.esBase()).isFalse();
    }

    @Test
    void elFactorDebeSerPositivo() {
        assertThatThrownBy(() -> new Unidad("x", "Nada", Dimension.MASA, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("factor");
    }

    @Test
    void elFactorAdmiteHastaSeisDecimales() {
        assertThatThrownBy(
                        () -> new Unidad("x", "Nada", Dimension.MASA, new BigDecimal("1.1234567")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("6 decimales");
    }

    @Test
    void elCodigoEsObligatorio() {
        assertThatThrownBy(() -> new Unidad(" ", "Nada", Dimension.MASA, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("código");
    }
}
