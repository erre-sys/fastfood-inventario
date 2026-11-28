package ec.com.erre.inventory.infrastructure.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UnitSpecificationsTest {

    @Test
    void wildcardsTypedByAPersonAreSearchedLiterally() {
        assertThat(UnitSpecifications.escapeLike("50%_off\\")).isEqualTo("50\\%\\_off\\\\");
    }

    @Test
    void plainTextIsUnchanged() {
        assertThat(UnitSpecifications.escapeLike("taza")).isEqualTo("taza");
    }
}
