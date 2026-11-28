package ec.com.erre.inventory.domain;

import java.util.UUID;

/**
 * Grouping of items, as in the costing spreadsheet (Vegetales, Abarrotes, Lácteos...). Never
 * deleted: an inactive category keeps its items, but new items cannot use it.
 *
 * @param audit who created and changed it; null for a category that has not been stored yet
 */
public record ItemCategory(UUID id, String name, boolean active, Audit audit) {

    public static final int NAME_MAX = 60;

    public ItemCategory {
        if (id == null) {
            throw new IllegalArgumentException("Category id is required");
        }
        if (name == null || name.isBlank() || name.length() > NAME_MAX) {
            throw new IllegalArgumentException(
                    "Category name is required, up to " + NAME_MAX + " characters");
        }
    }

    /** A new, active category. */
    public static ItemCategory create(UUID id, String name) {
        return new ItemCategory(id, name, true, null);
    }

    public ItemCategory withName(String newName) {
        return new ItemCategory(id, newName, active, audit);
    }

    public ItemCategory withActive(boolean newActive) {
        return new ItemCategory(id, name, newActive, audit);
    }
}
