package ec.com.erre.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.InvalidConversionException;
import ec.com.erre.inventory.domain.Unit;
import ec.com.erre.inventory.domain.UnitAlreadyExistsException;
import ec.com.erre.inventory.domain.UnitNotFoundException;
import ec.com.erre.inventory.domain.port.UnitSearch;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UnitServiceTest {

    private static final Unit GRAM = standard("g", "Gramo", Dimension.MASS, "1");
    private static final Unit KILOGRAM = standard("kg", "Kilogramo", Dimension.MASS, "1000");
    private static final Unit LITER = standard("l", "Litro", Dimension.VOLUME, "1000");

    private final InMemoryUnitRepository repository =
            new InMemoryUnitRepository().with(GRAM, KILOGRAM, LITER);
    private final UnitService service = new UnitService(repository);

    private static Unit standard(String code, String name, Dimension dimension, String factor) {
        return new Unit(code, name, dimension, new BigDecimal(factor), true, true);
    }

    private static NewUnit portion() {
        return new NewUnit("portion180", "Porción de 180 g", Dimension.MASS, new BigDecimal("180"));
    }

    @Test
    void createsABusinessUnitActiveAndNotStandard() {
        var created = service.create(portion());

        assertThat(created.code()).isEqualTo("portion180");
        assertThat(created.active()).isTrue();
        assertThat(created.standard()).isFalse();
        assertThat(repository.existsByCode("portion180")).isTrue();
    }

    @Test
    void aCodeCannotBeUsedTwice() {
        service.create(portion());
        var again = portion();

        assertThatThrownBy(() -> service.create(again))
                .isInstanceOf(UnitAlreadyExistsException.class)
                .hasMessageContaining("portion180");
    }

    @Test
    void getsAUnitByItsCode() {
        assertThat(service.get("kg")).isEqualTo(KILOGRAM);
    }

    @Test
    void anUnknownCodeIsNotFound() {
        assertThatThrownBy(() -> service.get("onza"))
                .isInstanceOf(UnitNotFoundException.class)
                .hasMessageContaining("onza");
    }

    @Test
    void renamesAStandardUnit() {
        var updated = service.update("kg", new UnitChanges("Kilo", null));

        assertThat(updated.name()).isEqualTo("Kilo");
        assertThat(updated.active()).isTrue();
        assertThat(updated.standard()).isTrue();
        assertThat(updated.baseFactor()).isEqualByComparingTo("1000");
    }

    @Test
    void inactivatesAUnitWithoutDeletingIt() {
        var updated = service.update("l", new UnitChanges(null, false));

        assertThat(updated.active()).isFalse();
        assertThat(service.get("l").name()).isEqualTo("Litro");
    }

    @Test
    void changesNameAndStateTogether() {
        service.create(portion());

        var updated = service.update("portion180", new UnitChanges("Porción grande", false));

        assertThat(updated.name()).isEqualTo("Porción grande");
        assertThat(updated.active()).isFalse();
    }

    @Test
    void noChangesLeaveTheUnitAsItWas() {
        assertThat(service.update("g", new UnitChanges(null, null))).isEqualTo(GRAM);
    }

    @Test
    void updatingAnUnknownCodeIsNotFound() {
        var changes = new UnitChanges("X", null);

        assertThatThrownBy(() -> service.update("onza", changes))
                .isInstanceOf(UnitNotFoundException.class);
    }

    @Test
    void anEmptyNameIsRejected() {
        var changes = new UnitChanges(" ", null);

        assertThatThrownBy(() -> service.update("kg", changes))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void convertsBetweenUnitsOfTheSameDimension() {
        var conversion = service.convert("kg", "g", new BigDecimal("2.5"));

        assertThat(conversion.source().value()).isEqualByComparingTo("2.5");
        assertThat(conversion.source().unit()).isEqualTo(KILOGRAM);
        assertThat(conversion.target().value()).isEqualTo(new BigDecimal("2500.000"));
        assertThat(conversion.target().unit()).isEqualTo(GRAM);
    }

    @Test
    void doesNotConvertBetweenDimensions() {
        var value = new BigDecimal("1");

        assertThatThrownBy(() -> service.convert("kg", "l", value))
                .isInstanceOf(InvalidConversionException.class);
    }

    @Test
    void convertingFromAnUnknownUnitIsNotFound() {
        var value = new BigDecimal("1");

        assertThatThrownBy(() -> service.convert("onza", "g", value))
                .isInstanceOf(UnitNotFoundException.class);
    }

    @Test
    void anInactiveUnitStillConverts() {
        service.update("kg", new UnitChanges(null, false));

        var conversion = service.convert("kg", "g", new BigDecimal("1"));

        assertThat(conversion.target().value()).isEqualByComparingTo("1000");
    }

    @Test
    void listsAPageOfUnits() {
        var page = service.list(UnitSearch.firstPage());

        assertThat(page.content()).extracting(Unit::code).containsExactly("g", "kg", "l");
        assertThat(page.totalElements()).isEqualTo(3);
    }
}
