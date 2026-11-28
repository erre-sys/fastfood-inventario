package ec.com.erre.inventory.domain;

import java.math.BigDecimal;

/**
 * Unit of measure. The factor says how many base units it holds: 1 kg = 1000 g.
 *
 * @param code short, stable identifier (kg, ml, unit); never changes
 * @param baseFactor amount of the base unit of its dimension; up to 6 decimal places
 * @param standard true for the units every install ships with
 * @param active false when inactivated; units are never deleted
 */
public record Unit(
        String code,
        String name,
        Dimension dimension,
        BigDecimal baseFactor,
        boolean standard,
        boolean active) {

    static final int FACTOR_SCALE = 6;

    public Unit {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Unit code is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Unit name is required");
        }
        if (dimension == null) {
            throw new IllegalArgumentException("Unit dimension is required");
        }
        if (baseFactor == null || baseFactor.signum() <= 0) {
            throw new IllegalArgumentException("Base factor must be positive");
        }
        if (baseFactor.stripTrailingZeros().scale() > FACTOR_SCALE) {
            throw new IllegalArgumentException(
                    "Base factor allows up to " + FACTOR_SCALE + " decimal places");
        }
    }

    /** A new business-specific unit: not standard, active. */
    public Unit(String code, String name, Dimension dimension, BigDecimal baseFactor) {
        this(code, name, dimension, baseFactor, false, true);
    }

    /** The same unit with another name. Only the name and the state may change after creation. */
    public Unit withName(String newName) {
        return new Unit(code, newName, dimension, baseFactor, standard, active);
    }

    /** The same unit, active or inactive. Units are never deleted. */
    public Unit withActive(boolean newActive) {
        return new Unit(code, name, dimension, baseFactor, standard, newActive);
    }

    public boolean isBase() {
        return baseFactor.compareTo(BigDecimal.ONE) == 0;
    }
}
