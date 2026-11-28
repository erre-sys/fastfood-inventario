package ec.com.erre.inventory.application;

/**
 * The changes of a merge patch: a null field is not changed. Only the name and the state can
 * change.
 */
public record UnitChanges(String name, Boolean active) {

    public boolean isEmpty() {
        return name == null && active == null;
    }
}
