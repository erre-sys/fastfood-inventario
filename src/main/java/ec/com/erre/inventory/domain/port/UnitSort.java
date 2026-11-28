package ec.com.erre.inventory.domain.port;

/** Fields a unit list can be sorted by. Anything else is rejected before reaching storage. */
public enum UnitSort {
    CODE,
    NAME,
    DIMENSION
}
