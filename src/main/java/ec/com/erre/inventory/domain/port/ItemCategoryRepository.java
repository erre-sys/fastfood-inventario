package ec.com.erre.inventory.domain.port;

import ec.com.erre.inventory.domain.ItemCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** What the domain needs to store and read item categories. Implemented by the infrastructure. */
public interface ItemCategoryRepository {

    /** Sorted by name; a null state returns active and inactive ones. */
    List<ItemCategory> findAll(Boolean active);

    Optional<ItemCategory> findById(UUID id);

    /**
     * True if a category other than {@code excludedId} (may be null) has this name, ignoring case.
     */
    boolean existsByName(String name, UUID excludedId);

    /** Creates the category, or updates an existing one: only its name and state change. */
    ItemCategory save(ItemCategory category);
}
