package ec.com.erre.inventory.domain;

/** A conversion was attempted between units of different dimensions (for example, kg to ml). */
public class InvalidConversionException extends RuntimeException {

    public InvalidConversionException(Unit source, Unit target) {
        super(
                "Cannot convert %s (%s) to %s (%s)"
                        .formatted(
                                source.code(),
                                source.dimension(),
                                target.code(),
                                target.dimension()));
    }
}
