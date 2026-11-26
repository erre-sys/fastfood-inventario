package ec.com.erre.fastfood.inventario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CantidadTest {

    private static final Unidad GRAMO = new Unidad("g", "Gramo", Dimension.MASA, bd("1"));
    private static final Unidad KILO = new Unidad("kg", "Kilogramo", Dimension.MASA, bd("1000"));
    private static final Unidad ONZA = new Unidad("oz", "Onza", Dimension.MASA, bd("28.349523"));
    private static final Unidad LITRO = new Unidad("l", "Litro", Dimension.VOLUMEN, bd("1000"));

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void convierteKilosAGramos() {
        var cantidad = new Cantidad(bd("2.5"), KILO);

        assertThat(cantidad.convertirA(GRAMO).valor()).isEqualByComparingTo("2500");
    }

    @Test
    void convierteGramosAKilos() {
        var cantidad = new Cantidad(bd("750"), GRAMO);

        assertThat(cantidad.convertirA(KILO).valor()).isEqualByComparingTo("0.75");
    }

    @Test
    void redondeaATresDecimales() {
        var cantidad = new Cantidad(bd("1"), ONZA);

        assertThat(cantidad.convertirA(GRAMO).valor()).isEqualTo(bd("28.350"));
    }

    @Test
    void laCantidadConvertidaLlevaLaUnidadDeDestino() {
        var cantidad = new Cantidad(bd("1"), KILO);

        assertThat(cantidad.convertirA(GRAMO).unidad()).isEqualTo(GRAMO);
    }

    @Test
    void noConvierteEntreDimensionesDistintas() {
        var cantidad = new Cantidad(bd("1"), KILO);

        assertThatThrownBy(() -> cantidad.convertirA(LITRO))
                .isInstanceOf(ConversionInvalidaException.class)
                .hasMessageContaining("MASA")
                .hasMessageContaining("VOLUMEN");
    }

    @Test
    void noAdmiteCantidadesNegativas() {
        var negativo = bd("-1");

        assertThatThrownBy(() -> new Cantidad(negativo, KILO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void expresaLaCantidadEnLaUnidadBase() {
        var cantidad = new Cantidad(bd("1.25"), KILO);

        assertThat(cantidad.enBase()).isEqualByComparingTo("1250");
    }
}
