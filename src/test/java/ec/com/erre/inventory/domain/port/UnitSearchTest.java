package ec.com.erre.inventory.domain.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class UnitSearchTest {

    @Test
    void theFirstPageHasSensibleDefaults() {
        var search = UnitSearch.firstPage();

        assertThat(search.page()).isZero();
        assertThat(search.size()).isEqualTo(UnitSearch.DEFAULT_SIZE);
        assertThat(search.sort()).isEqualTo(UnitSort.CODE);
        assertThat(search.ascending()).isTrue();
    }

    @Test
    void theSizeIsCapped() {
        assertThatThrownBy(() -> new UnitSearch(null, null, null, 0, 101, UnitSort.CODE, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100");
    }

    @Test
    void thePageCannotBeNegative() {
        assertThatThrownBy(() -> new UnitSearch(null, null, null, -1, 20, UnitSort.CODE, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aBlankTextDoesNotFilterAndAMissingSortIsByCode() {
        var search = new UnitSearch(null, "  ", null, 0, 20, null, true);

        assertThat(search.text()).isNull();
        assertThat(search.sort()).isEqualTo(UnitSort.CODE);
    }

    @Test
    void totalPagesRoundUp() {
        assertThat(new ResultPage<>(List.of(), 0, 5, 12).totalPages()).isEqualTo(3);
        assertThat(new ResultPage<>(List.of(), 0, 5, 10).totalPages()).isEqualTo(2);
        assertThat(new ResultPage<>(List.of(), 0, 5, 0).totalPages()).isZero();
    }
}
