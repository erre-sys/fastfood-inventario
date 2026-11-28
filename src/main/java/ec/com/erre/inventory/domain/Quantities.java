package ec.com.erre.inventory.domain;

import java.math.BigDecimal;

/** Shared checks for stored quantities: up to 12 integer digits and 3 decimal places. */
final class Quantities {

    static final int SCALE = 3;
    private static final int INTEGER_DIGITS = 12;

    private Quantities() {}

    /** True if the value fits the stored precision: NUMERIC(15, 3). */
    static boolean fits(BigDecimal value) {
        var plain = value.stripTrailingZeros();
        return plain.scale() <= SCALE && plain.precision() - plain.scale() <= INTEGER_DIGITS;
    }
}
