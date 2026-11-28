package ec.com.erre.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ItemCategoryTest {

    @Test
    void aNewCategoryIsActive() {
        var dairy = ItemCategory.create(UUID.randomUUID(), "Lácteos");

        assertThat(dairy.active()).isTrue();
        assertThat(dairy.withActive(false).withName("Lácteos y huevos"))
                .extracting(ItemCategory::name, ItemCategory::active)
                .containsExactly("Lácteos y huevos", false);
    }

    @Test
    void theNameIsRequiredUpToSixtyCharacters() {
        var id = UUID.randomUUID();

        assertThatThrownBy(() -> ItemCategory.create(id, ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ItemCategory.create(id, "x".repeat(61)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
