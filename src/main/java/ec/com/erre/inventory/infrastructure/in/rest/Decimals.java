package ec.com.erre.inventory.infrastructure.in.rest;

import java.math.BigDecimal;

/** Decimal values travel as strings to keep their exact precision (contract: {@code Decimal}). */
final class Decimals {

    private Decimals() {}

    /** Plain notation without trailing zeros: 1000.000000 becomes "1000", 0.001000 "0.001". */
    static String compact(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    /** Plain notation keeping the scale: a conversion result keeps its 3 decimal places. */
    static String plain(BigDecimal value) {
        return value.toPlainString();
    }
}
