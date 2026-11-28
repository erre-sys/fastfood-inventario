package ec.com.erre.inventory.domain;

/** Another item already has the given name (names are unique, ignoring case). */
public class ItemAlreadyExistsException extends RuntimeException {

    private final String name;

    public ItemAlreadyExistsException(String name) {
        super("Item already exists: " + name);
        this.name = name;
    }

    public String name() {
        return name;
    }
}
