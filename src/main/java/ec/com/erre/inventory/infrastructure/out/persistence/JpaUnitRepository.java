package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.Unit;
import ec.com.erre.inventory.domain.port.UnitRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Adapter: implements the domain port with JPA. */
@Repository
class JpaUnitRepository implements UnitRepository {

    private final UnitJpaRepository jpa;

    JpaUnitRepository(UnitJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Unit> findAll() {
        return jpa.findAll(Sort.by("dimension", "baseFactor")).stream()
                .map(UnitEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Unit> findByCode(String code) {
        return jpa.findById(code).map(UnitEntity::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsById(code);
    }

    /**
     * Creates the unit, or updates an existing one. On update only the name and the state change,
     * so the creation audit and the immutable fields are never overwritten.
     */
    @Override
    public Unit save(Unit unit) {
        var entity =
                jpa.findById(unit.code())
                        .map(existing -> {
                            existing.update(unit);
                            return existing;
                        })
                        .orElseGet(() -> UnitEntity.from(unit));
        return jpa.save(entity).toDomain();
    }
}
