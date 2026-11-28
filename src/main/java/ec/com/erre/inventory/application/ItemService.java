package ec.com.erre.inventory.application;

import ec.com.erre.inventory.application.InvalidFieldException.Reason;
import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.Item;
import ec.com.erre.inventory.domain.ItemAlreadyExistsException;
import ec.com.erre.inventory.domain.ItemCategory;
import ec.com.erre.inventory.domain.ItemNotFoundException;
import ec.com.erre.inventory.domain.Yield;
import ec.com.erre.inventory.domain.port.ItemCategoryRepository;
import ec.com.erre.inventory.domain.port.ItemRepository;
import ec.com.erre.inventory.domain.port.ItemSearch;
import ec.com.erre.inventory.domain.port.ResultPage;
import ec.com.erre.inventory.domain.port.UnitRepository;
import ec.com.erre.inventory.domain.port.YieldRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use cases of items and their yields. Permissions are checked by the REST adapter. */
@Service
@Transactional(readOnly = true)
public class ItemService {

    static final String CATEGORY = "categoryId";
    static final String STOCK_UNIT = "stockUnit";
    static final String GRAMS_PER_UNIT = "gramsPerUnit";
    static final String USABLE_QUANTITY = "usableQuantity";

    private final ItemRepository items;
    private final ItemCategoryRepository categories;
    private final UnitRepository units;
    private final YieldRepository yields;

    public ItemService(
            ItemRepository items,
            ItemCategoryRepository categories,
            UnitRepository units,
            YieldRepository yields) {
        this.items = items;
        this.categories = categories;
        this.units = units;
        this.yields = yields;
    }

    public ResultPage<Item> list(ItemSearch search) {
        return items.search(search);
    }

    public Item get(UUID id) {
        return items.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }

    /** The category and the stock unit must exist and be active. */
    @Transactional
    public Item create(NewItem newItem) {
        var name = newItem.name().strip();
        if (items.existsByName(name, null)) {
            throw new ItemAlreadyExistsException(name);
        }
        var category = activeCategory(newItem.categoryId());
        var unit =
                units.findByCode(newItem.stockUnit())
                        .orElseThrow(() -> new InvalidFieldException(STOCK_UNIT, Reason.NOT_FOUND));
        if (!unit.active()) {
            throw new InvalidFieldException(STOCK_UNIT, Reason.INACTIVE);
        }
        checkGramsPerUnit(unit.dimension(), newItem.gramsPerUnit());
        return items.save(
                Item.create(
                        UUID.randomUUID(),
                        name,
                        newItem.kind(),
                        category,
                        unit,
                        newItem.minimumStock(),
                        newItem.gramsPerUnit()));
    }

    /** Applies a merge patch. A new category must be active; the kind and the unit never change. */
    @Transactional
    public Item update(UUID id, ItemChanges changes) {
        var item = get(id);
        if (changes.isEmpty()) {
            return item;
        }
        if (changes.name() != null) {
            var name = changes.name().strip();
            if (items.existsByName(name, id)) {
                throw new ItemAlreadyExistsException(name);
            }
            item = item.withName(name);
        }
        if (changes.categoryId() != null && !changes.categoryId().equals(item.category().id())) {
            item = item.withCategory(activeCategory(changes.categoryId()));
        }
        if (changes.minimumStock() != null) {
            item = item.withMinimumStock(changes.minimumStock());
        }
        var grams = changes.gramsPerUnit().applyTo(item.gramsPerUnit());
        checkGramsPerUnit(item.dimension(), grams);
        item = item.withGramsPerUnit(grams);
        if (changes.active() != null) {
            item = item.withActive(changes.active());
        }
        return items.save(item);
    }

    /** Every yield measurement of the item, newest first. */
    public List<Yield> yields(UUID itemId) {
        get(itemId);
        return yields.findByItem(itemId);
    }

    /** Records a measurement; it becomes the current yield of the item. */
    @Transactional
    public Yield recordYield(UUID itemId, NewYield newYield) {
        var item = get(itemId);
        if (newYield.usableQuantity().compareTo(newYield.grossQuantity()) > 0) {
            throw new InvalidFieldException(USABLE_QUANTITY, Reason.EXCEEDS_GROSS);
        }
        var note =
                newYield.note() == null || newYield.note().isBlank()
                        ? null
                        : newYield.note().strip();
        var measured =
                Yield.measure(
                        UUID.randomUUID(),
                        itemId,
                        newYield.grossQuantity(),
                        newYield.usableQuantity(),
                        note);
        var saved = yields.save(measured);
        items.save(item.withYield(saved));
        return saved;
    }

    private ItemCategory activeCategory(UUID id) {
        var category =
                categories
                        .findById(id)
                        .orElseThrow(() -> new InvalidFieldException(CATEGORY, Reason.NOT_FOUND));
        if (!category.active()) {
            throw new InvalidFieldException(CATEGORY, Reason.INACTIVE);
        }
        return category;
    }

    private static void checkGramsPerUnit(Dimension dimension, BigDecimal grams) {
        if (grams != null && dimension != Dimension.COUNT) {
            throw new InvalidFieldException(GRAMS_PER_UNIT, Reason.NOT_APPLICABLE);
        }
    }
}
