package ec.com.erre.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class YieldTest {

    private static Yield measure(String gross, String usable) {
        return Yield.measure(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal(gross),
                new BigDecimal(usable),
                "Cebolla pelada");
    }

    @Test
    void theYieldIsTheUsablePartOfTheGrossQuantity() {
        var onion = measure("1000", "910");

        assertThat(onion.yieldPercent()).isEqualByComparingTo("91.00");
        assertThat(onion.wastePercent()).isEqualByComparingTo("9.00");
    }

    @Test
    void theYieldIsRoundedHalfUpToTwoDecimals() {
        assertThat(measure("3", "2").yieldPercent()).isEqualByComparingTo("66.67");
        assertThat(measure("8", "1").yieldPercent()).isEqualByComparingTo("12.50");
    }

    @Test
    void theUsablePartCannotExceedTheGrossQuantity() {
        assertThatThrownBy(() -> measure("100", "100.001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceed");
    }

    @Test
    void bothQuantitiesMustBePositive() {
        assertThatThrownBy(() -> measure("0", "0")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> measure("10", "0")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aStoredYieldMustMatchItsQuantities() {
        assertThatThrownBy(() ->
                        new Yield(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                new BigDecimal("1000"),
                                new BigDecimal("910"),
                                new BigDecimal("95"),
                                null,
                                null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theNoteIsOptionalUpToTwoHundredCharacters() {
        assertThatThrownBy(() ->
                        Yield.measure(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                BigDecimal.TEN,
                                BigDecimal.ONE,
                                "x".repeat(201)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
