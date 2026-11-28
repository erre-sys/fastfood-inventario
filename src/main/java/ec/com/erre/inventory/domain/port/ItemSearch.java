package ec.com.erre.inventory.domain.port;

import ec.com.erre.inventory.domain.ItemKind;
import java.util.UUID;

/**
 * A page of items, optionally filtered. Null filters do not filter.
 *
 * @param text case-insensitive match on the name
 * @param categoryId only the items of this category
 * @param page zero-based page number
 * @param size page size, from 1 to {@value UnitSearch#MAX_SIZE}
 */
public record ItemSearch(
        String text,
        ItemKind kind,
        UUID categoryId,
        Boolean active,
        int page,
        int size,
        ItemSort sort,
        boolean ascending) {

    public ItemSearch {
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        if (size < 1 || size > UnitSearch.MAX_SIZE) {
            throw new IllegalArgumentException("Size must be between 1 and " + UnitSearch.MAX_SIZE);
        }
        if (sort == null) {
            sort = ItemSort.NAME;
        }
        if (text != null && text.isBlank()) {
            text = null;
        }
    }

    /** First page, default size, every item, sorted by name. */
    public static ItemSearch firstPage() {
        return new ItemSearch(
                null, null, null, null, 0, UnitSearch.DEFAULT_SIZE, ItemSort.NAME, true);
    }
}
