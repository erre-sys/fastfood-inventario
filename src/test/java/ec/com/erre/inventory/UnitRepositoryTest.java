package ec.com.erre.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.Unit;
import ec.com.erre.inventory.domain.port.UnitRepository;
import ec.com.erre.inventory.domain.port.UnitSearch;
import ec.com.erre.inventory.domain.port.UnitSort;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/** The migrations and the JPA adapter, against a real PostgreSQL (Testcontainers). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class UnitRepositoryTest {

    @Autowired UnitRepository repository;
    @Autowired EntityManager entityManager;
    @Autowired JdbcClient jdbc;

    private static Unit portion(String name) {
        return new Unit("portion180", name, Dimension.MASS, new BigDecimal("180"));
    }

    private static void signIn(String username) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                username, null, List.of()));
    }

    /** Reads the audit columns straight from the table, after sending pending changes to it. */
    private Map<String, Object> auditOf(String code) {
        entityManager.flush();
        return jdbc.sql(
                        "SELECT created_by, created_at, updated_by, updated_at FROM unit WHERE code = ?")
                .param(code)
                .query()
                .singleRow();
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void migrationLoadsTheTwelveStandardUnitsActive() {
        assertThat(repository.findAll())
                .hasSize(12)
                .allMatch(Unit::standard)
                .allMatch(Unit::active);
    }

    private static UnitSearch search(Dimension dimension, String text, Boolean active) {
        return new UnitSearch(dimension, text, active, 0, 20, UnitSort.CODE, true);
    }

    @Test
    void searchesByDimension() {
        var page = repository.search(search(Dimension.MASS, null, null));

        assertThat(page.content())
                .extracting(Unit::code)
                .containsExactly("g", "kg", "lb", "mg", "oz");
    }

    @Test
    void searchesCodeOrNameIgnoringCase() {
        var page = repository.search(search(null, "LITRO", null));

        assertThat(page.content()).extracting(Unit::code).containsExactlyInAnyOrder("l", "ml");
    }

    @Test
    void searchesByState() {
        repository.save(repository.findByCode("cup").orElseThrow().withActive(false));

        assertThat(repository.search(search(null, null, false)).content())
                .extracting(Unit::code)
                .containsExactly("cup");
        assertThat(repository.search(search(null, null, true)).totalElements()).isEqualTo(11);
    }

    @Test
    void sortsAndPages() {
        var secondPage =
                repository.search(new UnitSearch(null, null, null, 1, 5, UnitSort.CODE, false));

        assertThat(secondPage.totalElements()).isEqualTo(12);
        assertThat(secondPage.totalPages()).isEqualTo(3);
        assertThat(secondPage.content())
                .extracting(Unit::code)
                .containsExactly("mg", "lb", "l", "kg", "g");
    }

    @Test
    void aSearchWithoutFiltersReturnsEveryUnit() {
        assertThat(repository.search(UnitSearch.firstPage()).totalElements()).isEqualTo(12);
    }

    @Test
    void findsAUnitByItsCode() {
        var kilogram = repository.findByCode("kg").orElseThrow();

        assertThat(kilogram.dimension()).isEqualTo(Dimension.MASS);
        assertThat(kilogram.baseFactor()).isEqualByComparingTo("1000");
    }

    @Test
    void savesABusinessSpecificUnitWithTheUserWhoCreatedIt() {
        signIn("eromero");

        var saved = repository.save(portion("Porción de 180 g"));

        assertThat(saved.standard()).isFalse();
        assertThat(saved.active()).isTrue();
        assertThat(auditOf("portion180"))
                .containsEntry("created_by", "eromero")
                .containsEntry("updated_by", null)
                .containsEntry("updated_at", null);
        assertThat(auditOf("portion180").get("created_at")).isNotNull();
    }

    @Test
    void aUnitSavedWithoutAUserIsAttributedToTheSystem() {
        repository.save(portion("Porción de 180 g"));

        assertThat(auditOf("portion180")).containsEntry("created_by", "system");
    }

    @Test
    void anUpdateRecordsWhoChangedItAndKeepsWhoCreatedIt() {
        signIn("eromero");
        repository.save(portion("Porción de 180 g"));
        auditOf("portion180");
        signIn("cajero1");

        var updated = repository.save(portion("Porción grande"));

        assertThat(updated.name()).isEqualTo("Porción grande");
        assertThat(auditOf("portion180"))
                .containsEntry("created_by", "eromero")
                .containsEntry("updated_by", "cajero1");
        assertThat(auditOf("portion180").get("updated_at")).isNotNull();
    }

    @Test
    void anUpdateOnlyChangesTheNameAndTheState() {
        repository.save(portion("Porción de 180 g"));
        var attempt =
                new Unit(
                        "portion180", "Otra", Dimension.VOLUME, new BigDecimal("999"), true, false);

        var updated = repository.save(attempt);

        assertThat(updated.name()).isEqualTo("Otra");
        assertThat(updated.active()).isFalse();
        assertThat(updated.dimension()).isEqualTo(Dimension.MASS);
        assertThat(updated.baseFactor()).isEqualByComparingTo("180");
        assertThat(updated.standard()).isFalse();
    }
}
