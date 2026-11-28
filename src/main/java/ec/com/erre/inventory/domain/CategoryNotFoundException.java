package ec.com.erre.inventory.domain;

import java.util.UUID;

/** No item category has the given id. */
public class CategoryNotFoundException extends RuntimeException {

    private final UUID id;

    public CategoryNotFoundException(UUID id) {
        super("Item category not found: " + id);
        this.id = id;
    }

    public UUID id() {
        return id;
    }
}
