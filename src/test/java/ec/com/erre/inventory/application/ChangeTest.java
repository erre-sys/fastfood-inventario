package ec.com.erre.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ChangeTest {

    @Test
    void keepRemoveAndSet() {
        assertThat(Change.<String>keep().applyTo("a")).isEqualTo("a");
        assertThat(Change.<String>remove().applyTo("a")).isNull();
        assertThat(Change.to("b").applyTo("a")).isEqualTo("b");
    }

    @Test
    void settingNullIsNotAllowed() {
        assertThatThrownBy(() -> Change.to(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
