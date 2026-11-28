package ec.com.erre.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UnitTest {

    @Test
    void aUnitWithFactorOneIsTheBaseOfItsDimension() {
        var gram = new Unit("g", "Gramo", Dimension.MASS, new BigDecimal("1"));
        var kilogram = new Unit("kg", "Kilogramo", Dimension.MASS, new BigDecimal("1000"));

        assertThat(gram.isBase()).isTrue();
        assertThat(kilogram.isBase()).isFalse();
    }

    @Test
    void aNewBusinessUnitIsActiveAndNotStandard() {
        var portion = new Unit("portion180", "Porción", Dimension.MASS, new BigDecimal("180"));

        assertThat(portion.active()).isTrue();
        assertThat(portion.standard()).isFalse();
    }

    @Test
    void factorMustBePositive() {
        assertThatThrownBy(() -> new Unit("x", "Nada", Dimension.MASS, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("factor");
    }

    @Test
    void factorAllowsUpToSixDecimalPlaces() {
        var tooPrecise = new BigDecimal("1.1234567");

        assertThatThrownBy(() -> new Unit("x", "Nada", Dimension.MASS, tooPrecise))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("6 decimal places");
    }

    @Test
    void codeIsRequired() {
        assertThatThrownBy(() -> new Unit(" ", "Nada", Dimension.MASS, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("code");
    }
}
