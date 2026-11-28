package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.Yield;
import ec.com.erre.inventory.domain.port.YieldRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Adapter: implements the yield port with JPA. Measurements are only inserted. */
@Repository
class JpaYieldRepository implements YieldRepository {

    private final YieldJpaRepository jpa;

    JpaYieldRepository(YieldJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Yield> findByItem(UUID itemId) {
        return jpa.findByItemIdOrderByCreatedAtDescIdDesc(itemId).stream()
                .map(YieldEntity::toDomain)
                .toList();
    }

    @Override
    public Yield save(Yield measured) {
        if (jpa.existsById(measured.id())) {
            throw new IllegalStateException("Yield measurements are never changed");
        }
        return jpa.saveAndFlush(YieldEntity.from(measured)).toDomain();
    }
}
