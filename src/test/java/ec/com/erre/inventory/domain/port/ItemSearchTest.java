package ec.com.erre.inventory.domain.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ItemSearchTest {

    @Test
    void theFirstPageSortsByName() {
        var search = ItemSearch.firstPage();

        assertThat(search.page()).isZero();
        assertThat(search.size()).isEqualTo(UnitSearch.DEFAULT_SIZE);
        assertThat(search.sort()).isEqualTo(ItemSort.NAME);
        assertThat(search.ascending()).isTrue();
    }

    @Test
    void aBlankTextDoesNotFilterAndANullSortIsByName() {
        var search = new ItemSearch(" ", null, null, null, 0, 10, null, true);

        assertThat(search.text()).isNull();
        assertThat(search.sort()).isEqualTo(ItemSort.NAME);
    }

    @Test
    void thePageSizeIsLimited() {
        assertThatThrownBy(() -> new ItemSearch(null, null, null, null, 0, 101, null, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ItemSearch(null, null, null, null, -1, 10, null, true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
