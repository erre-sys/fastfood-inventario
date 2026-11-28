package ec.com.erre.inventory.domain;

/** A conversion was attempted between units of different dimensions (for example, kg to ml). */
public class InvalidConversionException extends RuntimeException {

    private final transient Unit source;
    private final transient Unit target;

    public InvalidConversionException(Unit source, Unit target) {
        super(
                "Cannot convert %s (%s) to %s (%s)"
                        .formatted(
                                source.code(),
                                source.dimension(),
                                target.code(),
                                target.dimension()));
        this.source = source;
        this.target = target;
    }

    public Unit source() {
        return source;
    }

    public Unit target() {
        return target;
    }
}
