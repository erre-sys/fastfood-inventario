package ec.com.erre.inventory.domain.port;

import ec.com.erre.inventory.domain.Dimension;

/**
 * A page of units, optionally filtered. Null filters do not filter.
 *
 * @param text case-insensitive match on code or name
 * @param page zero-based page number
 * @param size page size, from 1 to {@value #MAX_SIZE}
 */
public record UnitSearch(
        Dimension dimension,
        String text,
        Boolean active,
        int page,
        int size,
        UnitSort sort,
        boolean ascending) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public UnitSearch {
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("Size must be between 1 and " + MAX_SIZE);
        }
        if (sort == null) {
            sort = UnitSort.CODE;
        }
        if (text != null && text.isBlank()) {
            text = null;
        }
    }

    /** First page, default size, every unit, sorted by code. */
    public static UnitSearch firstPage() {
        return new UnitSearch(null, null, null, 0, DEFAULT_SIZE, UnitSort.CODE, true);
    }
}
