package ec.com.erre.fastfood.inventory.infrastructure.out.persistence;

import ec.com.erre.fastfood.inventory.domain.Unit;
import ec.com.erre.fastfood.inventory.domain.port.UnitRepository;
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

    @Override
    public Unit save(Unit unit) {
        return jpa.save(UnitEntity.from(unit)).toDomain();
    }
}
