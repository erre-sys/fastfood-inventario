package ec.com.erre.fastfood.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import ec.com.erre.fastfood.inventory.domain.Dimension;
import ec.com.erre.fastfood.inventory.domain.Unit;
import ec.com.erre.fastfood.inventory.domain.port.UnitRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/** The migration and the JPA adapter, against a real PostgreSQL (Testcontainers). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class UnitRepositoryTest {

    @Autowired UnitRepository repository;

    @Test
    void migrationLoadsTheTwelveStandardUnits() {
        assertThat(repository.findAll()).hasSize(12);
    }

    @Test
    void findsAUnitByItsCode() {
        var kilogram = repository.findByCode("kg").orElseThrow();

        assertThat(kilogram.dimension()).isEqualTo(Dimension.MASS);
        assertThat(kilogram.baseFactor()).isEqualByComparingTo("1000");
    }

    @Test
    void savesABusinessSpecificUnit() {
        var portion =
                new Unit("portion180", "Porción de 180 g", Dimension.MASS, new BigDecimal("180"));

        repository.save(portion);

        assertThat(repository.existsByCode("portion180")).isTrue();
        assertThat(repository.findByCode("portion180").orElseThrow().name())
                .isEqualTo("Porción de 180 g");
    }
}
