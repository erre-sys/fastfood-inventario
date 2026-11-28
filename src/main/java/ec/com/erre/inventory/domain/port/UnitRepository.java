package ec.com.erre.inventory.domain.port;

import ec.com.erre.inventory.domain.Unit;
import java.util.List;
import java.util.Optional;

/** What the domain needs to store and read units. Implemented by the infrastructure. */
public interface UnitRepository {

    List<Unit> findAll();

    ResultPage<Unit> search(UnitSearch search);

    Optional<Unit> findByCode(String code);

    boolean existsByCode(String code);

    /** Creates the unit, or updates an existing one: only its name and state change. */
    Unit save(Unit unit);
}
