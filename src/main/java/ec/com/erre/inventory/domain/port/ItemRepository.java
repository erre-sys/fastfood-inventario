package ec.com.erre.inventory.domain.port;

import ec.com.erre.inventory.domain.Item;
import java.util.Optional;
import java.util.UUID;

/** What the domain needs to store and read items. Implemented by the infrastructure. */
public interface ItemRepository {

    ResultPage<Item> search(ItemSearch search);

    Optional<Item> findById(UUID id);

    /** True if an item other than {@code excludedId} (may be null) has this name, ignoring case. */
    boolean existsByName(String name, UUID excludedId);

    /**
     * Creates the item, or updates an existing one. The kind and the stock unit never change; the
     * category must exist.
     */
    Item save(Item item);
}
