package ec.com.erre.inventory.domain;

/** Another item category already has the given name (names are unique, ignoring case). */
public class CategoryAlreadyExistsException extends RuntimeException {

    private final String name;

    public CategoryAlreadyExistsException(String name) {
        super("Item category already exists: " + name);
        this.name = name;
    }

    public String name() {
        return name;
    }
}
