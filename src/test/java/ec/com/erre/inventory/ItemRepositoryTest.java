package ec.com.erre.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.Item;
import ec.com.erre.inventory.domain.ItemCategory;
import ec.com.erre.inventory.domain.ItemKind;
import ec.com.erre.inventory.domain.Yield;
import ec.com.erre.inventory.domain.port.ItemCategoryRepository;
import ec.com.erre.inventory.domain.port.ItemRepository;
import ec.com.erre.inventory.domain.port.ItemSearch;
import ec.com.erre.inventory.domain.port.ItemSort;
import ec.com.erre.inventory.domain.port.UnitRepository;
import ec.com.erre.inventory.domain.port.YieldRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/** Migration V3 and the item adapters, against a real PostgreSQL (Testcontainers). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ItemRepositoryTest {

    @Autowired ItemCategoryRepository categories;
    @Autowired ItemRepository items;
    @Autowired YieldRepository yields;
    @Autowired UnitRepository units;

    private ItemCategory dairy;
    private ItemCategory vegetables;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                "eromero", null, List.of()));
        dairy = categories.save(ItemCategory.create(UUID.randomUUID(), "Lácteos"));
        vegetables = categories.save(ItemCategory.create(UUID.randomUUID(), "Vegetales"));
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    private Item item(String name, ItemKind kind, ItemCategory category, String unit) {
        return items.save(
                Item.create(
                        UUID.randomUUID(),
                        name,
                        kind,
                        category,
                        units.findByCode(unit).orElseThrow(),
                        BigDecimal.ZERO,
                        null));
    }

    private static ItemSearch search(String text, ItemKind kind, UUID category, Boolean active) {
        return new ItemSearch(text, kind, category, active, 0, 20, ItemSort.NAME, true);
    }

    // --- Categories ---------------------------------------------------------------------------

    @Test
    void categoriesAreListedByNameAndFilteredByState() {
        categories.save(ItemCategory.create(UUID.randomUUID(), "Abarrotes").withActive(false));

        assertThat(categories.findAll(null))
                .extracting(ItemCategory::name)
                .containsExactly("Abarrotes", "Lácteos", "Vegetales");
        assertThat(categories.findAll(true))
                .extracting(ItemCategory::name)
                .containsExactly("Lácteos", "Vegetales");
    }

    @Test
    void aCategoryIsAuditedAndRenamed() {
        var renamed = categories.save(dairy.withName("Lácteos y huevos"));

        assertThat(renamed.name()).isEqualTo("Lácteos y huevos");
        assertThat(renamed.audit().createdBy()).isEqualTo("eromero");
        assertThat(renamed.audit().updatedBy()).isEqualTo("eromero");
        assertThat(renamed.audit().updatedAt()).isNotNull();
    }

    @Test
    void categoryNamesAreUniqueIgnoringCase() {
        assertThat(categories.existsByName("LÁCTEOS", null)).isTrue();
        assertThat(categories.existsByName("lácteos", dairy.id())).isFalse();
        assertThat(categories.existsByName("Proteínas", null)).isFalse();
    }

    // --- Items --------------------------------------------------------------------------------

    @Test
    void anItemIsStoredWithItsCategoryUnitAndAudit() {
        var egg =
                items.save(
                        Item.create(
                                UUID.randomUUID(),
                                "Huevo",
                                ItemKind.PURCHASED,
                                dairy,
                                units.findByCode("unit").orElseThrow(),
                                new BigDecimal("30"),
                                new BigDecimal("60")));

        var stored = items.findById(egg.id()).orElseThrow();
        assertThat(stored.name()).isEqualTo("Huevo");
        assertThat(stored.category()).isEqualTo(new Item.CategoryRef(dairy.id(), "Lácteos"));
        assertThat(stored.stockUnit()).isEqualTo("unit");
        assertThat(stored.dimension()).isEqualTo(Dimension.COUNT);
        assertThat(stored.minimumStock()).isEqualByComparingTo("30");
        assertThat(stored.gramsPerUnit()).isEqualByComparingTo("60");
        assertThat(stored.yieldPercent()).isEqualByComparingTo("100");
        assertThat(stored.audit().createdBy()).isEqualTo("eromero");
        assertThat(stored.audit().updatedAt()).isNull();
    }

    @Test
    void anUpdateChangesWhatMayChange() {
        var onion = item("Cebolla", ItemKind.PURCHASED, dairy, "g");

        var changed =
                items.save(
                        onion.withName("Cebolla paiteña")
                                .withCategory(vegetables)
                                .withMinimumStock(new BigDecimal("2000"))
                                .withActive(false));

        assertThat(changed.name()).isEqualTo("Cebolla paiteña");
        assertThat(changed.category().name()).isEqualTo("Vegetales");
        assertThat(changed.minimumStock()).isEqualByComparingTo("2000");
        assertThat(changed.active()).isFalse();
        assertThat(changed.stockUnit()).isEqualTo("g");
        assertThat(changed.audit().updatedBy()).isEqualTo("eromero");
    }

    @Test
    void itemNamesAreUniqueIgnoringCase() {
        var egg = item("Huevo", ItemKind.PURCHASED, dairy, "unit");

        assertThat(items.existsByName("HUEVO", null)).isTrue();
        assertThat(items.existsByName("huevo", egg.id())).isFalse();
        assertThatThrownBy(() -> item("huevo", ItemKind.PURCHASED, dairy, "unit"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void searchesByTextKindCategoryAndState() {
        item("Huevo", ItemKind.PURCHASED, dairy, "unit");
        item("Leche entera", ItemKind.PURCHASED, dairy, "ml");
        item("Cebolla", ItemKind.PURCHASED, vegetables, "g");
        item("Focaccia", ItemKind.PREPARED, vegetables, "unit");
        items.save(item("Leche deslactosada", ItemKind.PURCHASED, dairy, "ml").withActive(false));

        assertThat(items.search(search("LECHE", null, null, null)).content())
                .extracting(Item::name)
                .containsExactly("Leche deslactosada", "Leche entera");
        assertThat(items.search(search(null, ItemKind.PREPARED, null, null)).content())
                .extracting(Item::name)
                .containsExactly("Focaccia");
        assertThat(items.search(search(null, null, dairy.id(), true)).content())
                .extracting(Item::name)
                .containsExactly("Huevo", "Leche entera");
    }

    @Test
    void theTextIsSearchedLiterally() {
        item("Harina 100% integral", ItemKind.PURCHASED, vegetables, "g");
        item("Harina blanca", ItemKind.PURCHASED, vegetables, "g");

        assertThat(items.search(search("100%", null, null, null)).content())
                .extracting(Item::name)
                .containsExactly("Harina 100% integral");
    }

    @Test
    void sortsByKindThenNameAndPages() {
        item("Cebolla", ItemKind.PURCHASED, vegetables, "g");
        item("Focaccia", ItemKind.PREPARED, vegetables, "unit");
        item("Ajo", ItemKind.PURCHASED, vegetables, "g");
        item("Ragú", ItemKind.PREPARED, vegetables, "g");

        var page = items.search(new ItemSearch(null, null, null, null, 1, 2, ItemSort.KIND, false));

        // Descending kind: PURCHASED before PREPARED; then by name. Page 1 of size 2.
        assertThat(page.content()).extracting(Item::name).containsExactly("Focaccia", "Ragú");
        assertThat(page.totalElements()).isEqualTo(4);
        assertThat(page.totalPages()).isEqualTo(2);
    }

    // --- Yields -------------------------------------------------------------------------------

    @Test
    void yieldsAreKeptAsHistoryNewestFirstAndTheItemKeepsTheCurrentOne() {
        var onion = item("Cebolla", ItemKind.PURCHASED, vegetables, "g");
        var first =
                Yield.measure(
                        UUID.randomUUID(),
                        onion.id(),
                        new BigDecimal("1000"),
                        new BigDecimal("880"),
                        null);
        var second =
                Yield.measure(
                        UUID.randomUUID(),
                        onion.id(),
                        new BigDecimal("1000"),
                        new BigDecimal("910"),
                        "Cebolla paiteña pelada");
        yields.save(first);
        items.save(onion.withYield(first));
        yields.save(second);
        items.save(items.findById(onion.id()).orElseThrow().withYield(second));

        assertThat(yields.findByItem(onion.id()))
                .extracting(Yield::yieldPercent)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("91"), new BigDecimal("88"));
        assertThat(yields.findByItem(onion.id()).getFirst().audit().createdBy())
                .isEqualTo("eromero");
        assertThat(items.findById(onion.id()).orElseThrow().yieldPercent())
                .isEqualByComparingTo("91");
    }

    @Test
    void aYieldIsNeverChanged() {
        var onion = item("Cebolla", ItemKind.PURCHASED, vegetables, "g");
        var measured =
                yields.save(
                        Yield.measure(
                                UUID.randomUUID(),
                                onion.id(),
                                BigDecimal.TEN,
                                BigDecimal.ONE,
                                null));

        assertThatThrownBy(() -> yields.save(measured)).isInstanceOf(IllegalStateException.class);
    }
}
