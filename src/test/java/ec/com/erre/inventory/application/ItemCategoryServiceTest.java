package ec.com.erre.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.com.erre.inventory.domain.CategoryAlreadyExistsException;
import ec.com.erre.inventory.domain.CategoryNotFoundException;
import ec.com.erre.inventory.domain.ItemCategory;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ItemCategoryServiceTest {

    private static final ItemCategory DAIRY = ItemCategory.create(UUID.randomUUID(), "Lácteos");

    private final InMemoryItemCategoryRepository repository =
            new InMemoryItemCategoryRepository().with(DAIRY);
    private final ItemCategoryService service = new ItemCategoryService(repository);

    @Test
    void createsAnActiveCategoryWithoutSurroundingSpaces() {
        var created = service.create("  Vegetales ");

        assertThat(created.name()).isEqualTo("Vegetales");
        assertThat(created.active()).isTrue();
        assertThat(service.list(null))
                .extracting(ItemCategory::name)
                .containsExactly("Lácteos", "Vegetales");
    }

    @Test
    void aRepeatedNameIgnoringCaseIsRejected() {
        assertThatThrownBy(() -> service.create("LÁCTEOS"))
                .isInstanceOf(CategoryAlreadyExistsException.class);
    }

    @Test
    void renamesAndInactivates() {
        var changed = service.update(DAIRY.id(), new CategoryChanges("Lácteos y huevos", false));

        assertThat(changed.name()).isEqualTo("Lácteos y huevos");
        assertThat(changed.active()).isFalse();
        assertThat(service.list(true)).isEmpty();
    }

    @Test
    void keepingItsOwnNameIsNotADuplicate() {
        assertThat(service.update(DAIRY.id(), new CategoryChanges("lácteos", null)).name())
                .isEqualTo("lácteos");
    }

    @Test
    void renamingToAnotherCategoryNameIsRejected() {
        service.create("Vegetales");

        assertThatThrownBy(() -> service.update(DAIRY.id(), new CategoryChanges("vegetales", null)))
                .isInstanceOf(CategoryAlreadyExistsException.class);
    }

    @Test
    void aMissingCategoryIsNotFound() {
        assertThatThrownBy(() -> service.update(UUID.randomUUID(), new CategoryChanges("X", null)))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}
