package ec.com.erre.inventory.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** A value with its unit: 2.5 kg. Converts itself to another unit of the same dimension. */
public record Quantity(BigDecimal value, Unit unit) {

    static final int SCALE = 3;

    public Quantity {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException("Quantity must not be null or negative");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Quantity requires a unit");
        }
    }

    /** The value expressed in the base unit of its dimension (g, ml or unit). */
    public BigDecimal inBase() {
        return value.multiply(unit.baseFactor());
    }

    public Quantity convertTo(Unit target) {
        if (unit.dimension() != target.dimension()) {
            throw new InvalidConversionException(unit, target);
        }
        var converted = inBase().divide(target.baseFactor(), SCALE, RoundingMode.HALF_UP);
        return new Quantity(converted, target);
    }
}
