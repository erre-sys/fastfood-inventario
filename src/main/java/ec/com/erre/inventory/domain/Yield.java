package ec.com.erre.inventory.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * A yield measurement: of what was weighed (gross), how much can be used. The cost of the usable
 * part is the cost divided by the yield. Measurements are kept as history; the newest is current.
 *
 * @param grossQuantity what was weighed, in the stock unit of the item
 * @param usableQuantity the part that can be used, in the same unit; not above the gross quantity
 * @param yieldPercent usable ÷ gross × 100, rounded half-up to 2 decimals
 * @param audit who measured it and when; null before it is stored
 */
public record Yield(
        UUID id,
        UUID itemId,
        BigDecimal grossQuantity,
        BigDecimal usableQuantity,
        BigDecimal yieldPercent,
        String note,
        Audit audit) {

    static final int PERCENT_SCALE = 2;
    public static final int NOTE_MAX = 200;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public Yield {
        if (id == null || itemId == null) {
            throw new IllegalArgumentException("Yield id and item are required");
        }
        if (grossQuantity == null
                || grossQuantity.signum() <= 0
                || !Quantities.fits(grossQuantity)) {
            throw new IllegalArgumentException(
                    "Gross quantity must be positive, up to 3 decimal places");
        }
        if (usableQuantity == null
                || usableQuantity.signum() <= 0
                || !Quantities.fits(usableQuantity)) {
            throw new IllegalArgumentException(
                    "Usable quantity must be positive, up to 3 decimal places");
        }
        if (usableQuantity.compareTo(grossQuantity) > 0) {
            throw new IllegalArgumentException("Usable quantity cannot exceed the gross quantity");
        }
        if (yieldPercent == null
                || yieldPercent.compareTo(percent(grossQuantity, usableQuantity)) != 0) {
            throw new IllegalArgumentException("Yield percent must be usable ÷ gross × 100");
        }
        if (note != null && (note.isBlank() || note.length() > NOTE_MAX)) {
            throw new IllegalArgumentException("Note up to " + NOTE_MAX + " characters");
        }
    }

    /** A new measurement; the yield is computed from the two quantities. */
    public static Yield measure(
            UUID id,
            UUID itemId,
            BigDecimal grossQuantity,
            BigDecimal usableQuantity,
            String note) {
        if (grossQuantity == null || grossQuantity.signum() <= 0 || usableQuantity == null) {
            throw new IllegalArgumentException("Gross and usable quantities are required");
        }
        return new Yield(
                id,
                itemId,
                grossQuantity,
                usableQuantity,
                percent(grossQuantity, usableQuantity),
                note,
                null);
    }

    /** 100 − yield: the part lost (peel, bones, trimmings). */
    public BigDecimal wastePercent() {
        return HUNDRED.subtract(yieldPercent);
    }

    private static BigDecimal percent(BigDecimal gross, BigDecimal usable) {
        return usable.multiply(HUNDRED).divide(gross, PERCENT_SCALE, RoundingMode.HALF_UP);
    }
}
