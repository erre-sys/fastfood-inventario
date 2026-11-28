package ec.com.erre.inventory.application;

/** What a merge patch changes in a category. Null fields do not change. */
public record CategoryChanges(String name, Boolean active) {

    public boolean isEmpty() {
        return name == null && active == null;
    }
}
