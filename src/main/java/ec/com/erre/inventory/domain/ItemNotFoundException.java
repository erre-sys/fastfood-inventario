package ec.com.erre.inventory.domain;

import java.util.UUID;

/** No item has the given id. */
public class ItemNotFoundException extends RuntimeException {

    private final UUID id;

    public ItemNotFoundException(UUID id) {
        super("Item not found: " + id);
        this.id = id;
    }

    public UUID id() {
        return id;
    }
}
