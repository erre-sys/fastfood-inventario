package ec.com.erre.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ItemTest {

    private static final ItemCategory DAIRY = ItemCategory.create(UUID.randomUUID(), "Lácteos");
    private static final ItemCategory GROCERIES =
            ItemCategory.create(UUID.randomUUID(), "Abarrotes");
    private static final Unit UNIT = new Unit("unit", "Unidad", Dimension.COUNT, BigDecimal.ONE);
    private static final Unit GRAM = new Unit("g", "Gramo", Dimension.MASS, BigDecimal.ONE);

    private static Item egg() {
        return Item.create(
                UUID.randomUUID(),
                "Huevo",
                ItemKind.PURCHASED,
                DAIRY,
                UNIT,
                new BigDecimal("30"),
                new BigDecimal("60"));
    }

    @Test
    void aNewItemIsActiveWithFullYieldInTheDimensionOfItsUnit() {
        var egg = egg();

        assertThat(egg.active()).isTrue();
        assertThat(egg.yieldPercent()).isEqualByComparingTo("100");
        assertThat(egg.stockUnit()).isEqualTo("unit");
        assertThat(egg.dimension()).isEqualTo(Dimension.COUNT);
        assertThat(egg.category()).isEqualTo(new Item.CategoryRef(DAIRY.id(), "Lácteos"));
        assertThat(egg.audit()).isNull();
    }

    @Test
    void gramsPerUnitOnlyAppliesToItemsCountedInUnits() {
        assertThatThrownBy(() ->
                        Item.create(
                                UUID.randomUUID(),
                                "Harina",
                                ItemKind.PURCHASED,
                                GROCERIES,
                                GRAM,
                                BigDecimal.ZERO,
                                new BigDecimal("1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("counted in units");
    }

    @Test
    void theMinimumStockCannotBeNegativeNorHaveMoreThanThreeDecimals() {
        var egg = egg();

        assertThatThrownBy(() -> egg.withMinimumStock(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> egg.withMinimumStock(new BigDecimal("1.0001")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(egg.withMinimumStock(new BigDecimal("1.500")).minimumStock())
                .isEqualByComparingTo("1.5");
    }

    @Test
    void theNameIsRequiredUpToEightyCharacters() {
        var egg = egg();

        assertThatThrownBy(() -> egg.withName(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> egg.withName("x".repeat(81)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theChangesKeepTheKindAndTheStockUnit() {
        var changed =
                egg().withName("Huevo de campo")
                        .withCategory(GROCERIES)
                        .withGramsPerUnit(null)
                        .withActive(false);

        assertThat(changed.name()).isEqualTo("Huevo de campo");
        assertThat(changed.category().name()).isEqualTo("Abarrotes");
        assertThat(changed.gramsPerUnit()).isNull();
        assertThat(changed.active()).isFalse();
        assertThat(changed.kind()).isEqualTo(ItemKind.PURCHASED);
        assertThat(changed.stockUnit()).isEqualTo("unit");
    }

    @Test
    void aMeasuredYieldBecomesTheCurrentOne() {
        var egg = egg();
        var measured =
                Yield.measure(
                        UUID.randomUUID(),
                        egg.id(),
                        new BigDecimal("1000"),
                        new BigDecimal("910"),
                        null);

        assertThat(egg.withYield(measured).yieldPercent()).isEqualByComparingTo("91");
    }

    @Test
    void aYieldOfAnotherItemIsRejected() {
        var measured =
                Yield.measure(
                        UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, BigDecimal.ONE, null);

        assertThatThrownBy(() -> egg().withYield(measured))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
