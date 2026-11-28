package ec.com.erre.inventory.application;

import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.Unit;
import java.math.BigDecimal;

/** What a person provides to create a business-specific unit. */
public record NewUnit(String code, String name, Dimension dimension, BigDecimal baseFactor) {

    /** A new unit is never standard and starts active. */
    Unit toUnit() {
        return new Unit(code, name, dimension, baseFactor);
    }
}
