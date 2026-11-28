package ec.com.erre.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class QuantityTest {

    private static final Unit GRAM = new Unit("g", "Gramo", Dimension.MASS, bd("1"));
    private static final Unit KILOGRAM = new Unit("kg", "Kilogramo", Dimension.MASS, bd("1000"));
    private static final Unit OUNCE = new Unit("oz", "Onza", Dimension.MASS, bd("28.349523"));
    private static final Unit LITER = new Unit("l", "Litro", Dimension.VOLUME, bd("1000"));

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @Test
    void convertsKilogramsToGrams() {
        var quantity = new Quantity(bd("2.5"), KILOGRAM);

        assertThat(quantity.convertTo(GRAM).value()).isEqualByComparingTo("2500");
    }

    @Test
    void convertsGramsToKilograms() {
        var quantity = new Quantity(bd("750"), GRAM);

        assertThat(quantity.convertTo(KILOGRAM).value()).isEqualByComparingTo("0.75");
    }

    @Test
    void roundsToThreeDecimalPlaces() {
        var quantity = new Quantity(bd("1"), OUNCE);

        assertThat(quantity.convertTo(GRAM).value()).isEqualTo(bd("28.350"));
    }

    @Test
    void convertedQuantityCarriesTheTargetUnit() {
        var quantity = new Quantity(bd("1"), KILOGRAM);

        assertThat(quantity.convertTo(GRAM).unit()).isEqualTo(GRAM);
    }

    @Test
    void doesNotConvertBetweenDifferentDimensions() {
        var quantity = new Quantity(bd("1"), KILOGRAM);

        assertThatThrownBy(() -> quantity.convertTo(LITER))
                .isInstanceOf(InvalidConversionException.class)
                .hasMessageContaining("MASS")
                .hasMessageContaining("VOLUME");
    }

    @Test
    void rejectsNegativeQuantities() {
        var negative = bd("-1");

        assertThatThrownBy(() -> new Quantity(negative, KILOGRAM))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void expressesTheQuantityInTheBaseUnit() {
        var quantity = new Quantity(bd("1.25"), KILOGRAM);

        assertThat(quantity.inBase()).isEqualByComparingTo("1250");
    }
}
