package ec.com.erre.inventory.application;

import ec.com.erre.inventory.domain.ItemCategory;
import ec.com.erre.inventory.domain.port.ItemCategoryRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** A fake repository for the use case tests: no Spring, no database. */
class InMemoryItemCategoryRepository implements ItemCategoryRepository {

    private final Map<UUID, ItemCategory> categories = new LinkedHashMap<>();

    InMemoryItemCategoryRepository with(ItemCategory... initial) {
        for (ItemCategory category : initial) {
            categories.put(category.id(), category);
        }
        return this;
    }

    @Override
    public List<ItemCategory> findAll(Boolean active) {
        return categories.values().stream()
                .filter(category -> active == null || category.active() == active)
                .sorted(Comparator.comparing(ItemCategory::name))
                .toList();
    }

    @Override
    public Optional<ItemCategory> findById(UUID id) {
        return Optional.ofNullable(categories.get(id));
    }

    @Override
    public boolean existsByName(String name, UUID excludedId) {
        var lower = name.toLowerCase(Locale.ROOT);
        return categories.values().stream()
                .anyMatch(category ->
                        !category.id().equals(excludedId)
                                && category.name().toLowerCase(Locale.ROOT).equals(lower));
    }

    @Override
    public ItemCategory save(ItemCategory category) {
        categories.put(category.id(), category);
        return category;
    }
}
