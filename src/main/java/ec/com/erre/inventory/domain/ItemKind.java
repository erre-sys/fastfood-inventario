package ec.com.erre.inventory.domain;

/** How an item gets into stock. */
public enum ItemKind {
    /** Bought, including what is resold as it is (a soda). */
    PURCHASED,
    /** Comes out of a batch (focaccia, ragú) and has its own stock. */
    PREPARED
}
