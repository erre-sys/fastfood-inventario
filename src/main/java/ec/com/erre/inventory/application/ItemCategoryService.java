package ec.com.erre.inventory.application;

import ec.com.erre.inventory.domain.CategoryAlreadyExistsException;
import ec.com.erre.inventory.domain.CategoryNotFoundException;
import ec.com.erre.inventory.domain.ItemCategory;
import ec.com.erre.inventory.domain.port.ItemCategoryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use cases of item categories. Permissions are checked by the REST adapter. */
@Service
@Transactional(readOnly = true)
public class ItemCategoryService {

    private final ItemCategoryRepository categories;

    public ItemCategoryService(ItemCategoryRepository categories) {
        this.categories = categories;
    }

    /** Sorted by name; a null state returns active and inactive ones. */
    public List<ItemCategory> list(Boolean active) {
        return categories.findAll(active);
    }

    public ItemCategory get(UUID id) {
        return categories.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional
    public ItemCategory create(String name) {
        var clean = name.strip();
        if (categories.existsByName(clean, null)) {
            throw new CategoryAlreadyExistsException(clean);
        }
        return categories.save(ItemCategory.create(UUID.randomUUID(), clean));
    }

    /** Renames or (in)activates. An inactive category keeps its items. */
    @Transactional
    public ItemCategory update(UUID id, CategoryChanges changes) {
        var category = get(id);
        if (changes.isEmpty()) {
            return category;
        }
        if (changes.name() != null) {
            var clean = changes.name().strip();
            if (categories.existsByName(clean, id)) {
                throw new CategoryAlreadyExistsException(clean);
            }
            category = category.withName(clean);
        }
        if (changes.active() != null) {
            category = category.withActive(changes.active());
        }
        return categories.save(category);
    }
}
