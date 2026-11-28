package ec.com.erre.inventory.application;

import ec.com.erre.inventory.domain.Item;
import ec.com.erre.inventory.domain.port.ItemRepository;
import ec.com.erre.inventory.domain.port.ItemSearch;
import ec.com.erre.inventory.domain.port.ResultPage;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** A fake repository for the use case tests. Like the adapter, kind and unit never change. */
class InMemoryItemRepository implements ItemRepository {

    private final Map<UUID, Item> items = new LinkedHashMap<>();

    @Override
    public ResultPage<Item> search(ItemSearch search) {
        var matching = items.values().stream().sorted(Comparator.comparing(Item::name)).toList();
        var from = Math.min(search.page() * search.size(), matching.size());
        var to = Math.min(from + search.size(), matching.size());
        return new ResultPage<>(
                matching.subList(from, to), search.page(), search.size(), matching.size());
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public boolean existsByName(String name, UUID excludedId) {
        var lower = name.toLowerCase(Locale.ROOT);
        return items.values().stream()
                .anyMatch(item ->
                        !item.id().equals(excludedId)
                                && item.name().toLowerCase(Locale.ROOT).equals(lower));
    }

    @Override
    public Item save(Item item) {
        var stored = items.get(item.id());
        if (stored != null
                && (stored.kind() != item.kind() || !stored.stockUnit().equals(item.stockUnit()))) {
            throw new IllegalStateException("The kind and the stock unit never change");
        }
        items.put(item.id(), item);
        return item;
    }
}
