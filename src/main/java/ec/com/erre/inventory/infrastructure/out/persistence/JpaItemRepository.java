package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.Item;
import ec.com.erre.inventory.domain.port.ItemRepository;
import ec.com.erre.inventory.domain.port.ItemSearch;
import ec.com.erre.inventory.domain.port.ResultPage;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter: implements the item port with JPA. Transactional so the category and the stock unit
 * (lazy relations) can be read while mapping to the domain.
 */
@Repository
@Transactional(readOnly = true)
class JpaItemRepository implements ItemRepository {

    private final ItemJpaRepository items;
    private final ItemCategoryJpaRepository categories;
    private final UnitJpaRepository units;

    JpaItemRepository(
            ItemJpaRepository items,
            ItemCategoryJpaRepository categories,
            UnitJpaRepository units) {
        this.items = items;
        this.categories = categories;
        this.units = units;
    }

    /** Sorted by the requested field, then by name and id, so pages are stable. */
    @Override
    public ResultPage<Item> search(ItemSearch search) {
        var field =
                switch (search.sort()) {
                    case NAME -> "name";
                    case KIND -> "kind";
                };
        var direction = search.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
        var sort = Sort.by(direction, field).and(Sort.by("name")).and(Sort.by("id"));
        var page =
                items.findAll(
                        ItemSpecifications.matching(search),
                        PageRequest.of(search.page(), search.size(), sort));
        return new ResultPage<>(
                page.getContent().stream().map(ItemEntity::toDomain).toList(),
                search.page(),
                search.size(),
                page.getTotalElements());
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return items.findById(id).map(ItemEntity::toDomain);
    }

    @Override
    public boolean existsByName(String name, UUID excludedId) {
        return excludedId == null
                ? items.existsByNameIgnoreCase(name)
                : items.existsByNameIgnoreCaseAndIdNot(name, excludedId);
    }

    /**
     * Creates the item, or updates an existing one; the kind and the stock unit are only written on
     * creation. Flushes so the audit of an update is in the returned item.
     */
    @Override
    @Transactional
    public Item save(Item item) {
        var category =
                categories
                        .findById(item.category().id())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Category not found: " + item.category().id()));
        var entity =
                items.findById(item.id())
                        .map(existing -> {
                            existing.update(item, category);
                            return existing;
                        })
                        .orElseGet(() -> {
                            var unit =
                                    units.findById(item.stockUnit())
                                            .orElseThrow(() ->
                                                    new IllegalStateException(
                                                            "Unit not found: " + item.stockUnit()));
                            return ItemEntity.from(item, category, unit);
                        });
        return items.saveAndFlush(entity).toDomain();
    }
}
