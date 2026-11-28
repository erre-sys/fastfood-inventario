package ec.com.erre.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.com.erre.inventory.application.InvalidFieldException.Reason;
import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.Item;
import ec.com.erre.inventory.domain.ItemAlreadyExistsException;
import ec.com.erre.inventory.domain.ItemCategory;
import ec.com.erre.inventory.domain.ItemKind;
import ec.com.erre.inventory.domain.ItemNotFoundException;
import ec.com.erre.inventory.domain.Unit;
import ec.com.erre.inventory.domain.Yield;
import java.math.BigDecimal;
import java.util.UUID;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class ItemServiceTest {

    private static final ItemCategory DAIRY = ItemCategory.create(UUID.randomUUID(), "Lácteos");
    private static final ItemCategory VEGETABLES =
            ItemCategory.create(UUID.randomUUID(), "Vegetales");
    private static final ItemCategory OLD =
            ItemCategory.create(UUID.randomUUID(), "Antigua").withActive(false);
    private static final Unit UNIT =
            new Unit("unit", "Unidad", Dimension.COUNT, BigDecimal.ONE, true, true);
    private static final Unit GRAM =
            new Unit("g", "Gramo", Dimension.MASS, BigDecimal.ONE, true, true);
    private static final Unit CUP =
            new Unit("cup", "Taza", Dimension.VOLUME, new BigDecimal("240"), true, false);

    private final InMemoryItemRepository items = new InMemoryItemRepository();
    private final InMemoryYieldRepository yields = new InMemoryYieldRepository();
    private final ItemService service =
            new ItemService(
                    items,
                    new InMemoryItemCategoryRepository().with(DAIRY, VEGETABLES, OLD),
                    new InMemoryUnitRepository().with(UNIT, GRAM, CUP),
                    yields);

    private static NewItem egg() {
        return new NewItem(
                " Huevo ",
                ItemKind.PURCHASED,
                DAIRY.id(),
                "unit",
                new BigDecimal("30"),
                new BigDecimal("60"));
    }

    private Item onion() {
        return service.create(
                new NewItem(
                        "Cebolla",
                        ItemKind.PURCHASED,
                        VEGETABLES.id(),
                        "g",
                        BigDecimal.ZERO,
                        null));
    }

    private static void assertInvalid(ThrowingCallable call, String field, Reason reason) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(
                        InvalidFieldException.class,
                        e -> {
                            assertThat(e.field()).isEqualTo(field);
                            assertThat(e.reason()).isEqualTo(reason);
                        });
    }

    // --- Create -------------------------------------------------------------------------------

    @Test
    void createsAnActiveItemWithFullYield() {
        var created = service.create(egg());

        assertThat(created.name()).isEqualTo("Huevo");
        assertThat(created.category().name()).isEqualTo("Lácteos");
        assertThat(created.dimension()).isEqualTo(Dimension.COUNT);
        assertThat(created.yieldPercent()).isEqualByComparingTo("100");
        assertThat(created.active()).isTrue();
        assertThat(service.get(created.id())).isEqualTo(created);
    }

    @Test
    void aRepeatedNameIgnoringCaseIsRejected() {
        service.create(egg());

        assertThatThrownBy(() -> service.create(egg()))
                .isInstanceOf(ItemAlreadyExistsException.class);
    }

    @Test
    void theCategoryMustExistAndBeActive() {
        assertInvalid(
                () ->
                        service.create(
                                new NewItem(
                                        "X",
                                        ItemKind.PURCHASED,
                                        UUID.randomUUID(),
                                        "g",
                                        BigDecimal.ZERO,
                                        null)),
                "categoryId",
                Reason.NOT_FOUND);
        assertInvalid(
                () ->
                        service.create(
                                new NewItem(
                                        "X",
                                        ItemKind.PURCHASED,
                                        OLD.id(),
                                        "g",
                                        BigDecimal.ZERO,
                                        null)),
                "categoryId",
                Reason.INACTIVE);
    }

    @Test
    void theStockUnitMustExistAndBeActive() {
        assertInvalid(
                () ->
                        service.create(
                                new NewItem(
                                        "X",
                                        ItemKind.PURCHASED,
                                        DAIRY.id(),
                                        "saco",
                                        BigDecimal.ZERO,
                                        null)),
                "stockUnit",
                Reason.NOT_FOUND);
        assertInvalid(
                () ->
                        service.create(
                                new NewItem(
                                        "X",
                                        ItemKind.PURCHASED,
                                        DAIRY.id(),
                                        "cup",
                                        BigDecimal.ZERO,
                                        null)),
                "stockUnit",
                Reason.INACTIVE);
    }

    @Test
    void gramsPerUnitOnlyForItemsCountedInUnits() {
        assertInvalid(
                () ->
                        service.create(
                                new NewItem(
                                        "Harina",
                                        ItemKind.PURCHASED,
                                        DAIRY.id(),
                                        "g",
                                        BigDecimal.ZERO,
                                        BigDecimal.ONE)),
                "gramsPerUnit",
                Reason.NOT_APPLICABLE);
    }

    @Test
    void aMissingItemIsNotFound() {
        assertThatThrownBy(() -> service.get(UUID.randomUUID()))
                .isInstanceOf(ItemNotFoundException.class);
    }

    // --- Update -------------------------------------------------------------------------------

    @Test
    void appliesTheMergePatch() {
        var egg = service.create(egg());

        var changed =
                service.update(
                        egg.id(),
                        new ItemChanges(
                                "Huevo de campo",
                                VEGETABLES.id(),
                                new BigDecimal("45"),
                                Change.remove(),
                                false));

        assertThat(changed.name()).isEqualTo("Huevo de campo");
        assertThat(changed.category().id()).isEqualTo(VEGETABLES.id());
        assertThat(changed.minimumStock()).isEqualByComparingTo("45");
        assertThat(changed.gramsPerUnit()).isNull();
        assertThat(changed.active()).isFalse();
        assertThat(changed.kind()).isEqualTo(ItemKind.PURCHASED);
        assertThat(changed.stockUnit()).isEqualTo("unit");
    }

    @Test
    void missingFieldsDoNotChange() {
        var egg = service.create(egg());

        var changed =
                service.update(
                        egg.id(), new ItemChanges(null, null, new BigDecimal("10"), null, null));

        assertThat(changed.name()).isEqualTo("Huevo");
        assertThat(changed.gramsPerUnit()).isEqualByComparingTo("60");
        assertThat(changed.active()).isTrue();
    }

    @Test
    void theNewCategoryMustBeActive() {
        var egg = service.create(egg());

        assertInvalid(
                () -> service.update(egg.id(), new ItemChanges(null, OLD.id(), null, null, null)),
                "categoryId",
                Reason.INACTIVE);
    }

    @Test
    void anItemInAnInactiveCategoryCanStillBeChanged() {
        var egg = service.create(egg());
        // The category is inactivated later: the item keeps it and can still change
        var stored = items.save(service.get(egg.id()).withCategory(OLD));

        var changed =
                service.update(
                        stored.id(), new ItemChanges(null, OLD.id(), BigDecimal.ONE, null, null));

        assertThat(changed.category().id()).isEqualTo(OLD.id());
    }

    @Test
    void gramsPerUnitCannotBeSetOnAnItemWeighedInGrams() {
        var onion = onion();

        assertInvalid(
                () ->
                        service.update(
                                onion.id(),
                                new ItemChanges(null, null, null, Change.to(BigDecimal.TEN), null)),
                "gramsPerUnit",
                Reason.NOT_APPLICABLE);
    }

    @Test
    void renamingToAnotherItemNameIsRejected() {
        service.create(egg());
        var onion = onion();

        assertThatThrownBy(() ->
                        service.update(
                                onion.id(), new ItemChanges("HUEVO", null, null, null, null)))
                .isInstanceOf(ItemAlreadyExistsException.class);
    }

    // --- Yields -------------------------------------------------------------------------------

    @Test
    void aMeasurementBecomesTheCurrentYieldAndIsKeptAsHistory() {
        var onion = onion();

        service.recordYield(
                onion.id(), new NewYield(new BigDecimal("1000"), new BigDecimal("880"), null));
        var latest =
                service.recordYield(
                        onion.id(),
                        new NewYield(new BigDecimal("1000"), new BigDecimal("910"), " Paiteña "));

        assertThat(latest.yieldPercent()).isEqualByComparingTo("91");
        assertThat(latest.note()).isEqualTo("Paiteña");
        assertThat(service.get(onion.id()).yieldPercent()).isEqualByComparingTo("91");
        assertThat(service.yields(onion.id()))
                .extracting(Yield::yieldPercent)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("91"), new BigDecimal("88"));
    }

    @Test
    void theUsablePartCannotExceedTheGrossQuantity() {
        var onion = onion();

        assertInvalid(
                () ->
                        service.recordYield(
                                onion.id(), new NewYield(BigDecimal.ONE, BigDecimal.TEN, null)),
                "usableQuantity",
                Reason.EXCEEDS_GROSS);
    }

    @Test
    void aBlankNoteIsNoNote() {
        var onion = onion();

        assertThat(
                        service.recordYield(
                                        onion.id(),
                                        new NewYield(BigDecimal.TEN, BigDecimal.ONE, "  "))
                                .note())
                .isNull();
    }

    @Test
    void theYieldsOfAMissingItemAreNotFound() {
        assertThatThrownBy(() -> service.yields(UUID.randomUUID()))
                .isInstanceOf(ItemNotFoundException.class);
    }
}
