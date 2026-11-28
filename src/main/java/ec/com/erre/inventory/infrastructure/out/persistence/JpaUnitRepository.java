package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.Unit;
import ec.com.erre.inventory.domain.port.ResultPage;
import ec.com.erre.inventory.domain.port.UnitRepository;
import ec.com.erre.inventory.domain.port.UnitSearch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
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

    /** Sorted by the requested field, then by code, so pages are stable. */
    @Override
    public ResultPage<Unit> search(UnitSearch search) {
        var field =
                switch (search.sort()) {
                    case CODE -> "code";
                    case NAME -> "name";
                    case DIMENSION -> "dimension";
                };
        var direction = search.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
        var sort = Sort.by(direction, field).and(Sort.by("code"));
        var page =
                jpa.findAll(
                        UnitSpecifications.matching(search),
                        PageRequest.of(search.page(), search.size(), sort));
        return new ResultPage<>(
                page.getContent().stream().map(UnitEntity::toDomain).toList(),
                search.page(),
                search.size(),
                page.getTotalElements());
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
