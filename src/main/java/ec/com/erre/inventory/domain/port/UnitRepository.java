package ec.com.erre.inventory.domain.port;

import ec.com.erre.inventory.domain.Unit;
import java.util.List;
import java.util.Optional;

/** What the domain needs to store and read units. Implemented by the infrastructure. */
public interface UnitRepository {

    List<Unit> findAll();

    Optional<Unit> findByCode(String code);

    boolean existsByCode(String code);

    Unit save(Unit unit);
}
