package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.ItemCategory;
import ec.com.erre.inventory.domain.port.ItemCategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Adapter: implements the item category port with JPA. */
@Repository
class JpaItemCategoryRepository implements ItemCategoryRepository {

    private static final Sort BY_NAME = Sort.by("name");

    private final ItemCategoryJpaRepository jpa;

    JpaItemCategoryRepository(ItemCategoryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ItemCategory> findAll(Boolean active) {
        var rows = active == null ? jpa.findAll(BY_NAME) : jpa.findByActive(active, BY_NAME);
        return rows.stream().map(ItemCategoryEntity::toDomain).toList();
    }

    @Override
    public Optional<ItemCategory> findById(UUID id) {
        return jpa.findById(id).map(ItemCategoryEntity::toDomain);
    }

    @Override
    public boolean existsByName(String name, UUID excludedId) {
        return excludedId == null
                ? jpa.existsByNameIgnoreCase(name)
                : jpa.existsByNameIgnoreCaseAndIdNot(name, excludedId);
    }

    /** Flushes so the audit of an update is in the returned category. */
    @Override
    public ItemCategory save(ItemCategory category) {
        var entity =
                jpa.findById(category.id())
                        .map(existing -> {
                            existing.update(category);
                            return existing;
                        })
                        .orElseGet(() -> ItemCategoryEntity.from(category));
        return jpa.saveAndFlush(entity).toDomain();
    }
}
