package ec.com.erre.inventory.application;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * What a merge patch changes in an item. Null fields do not change; {@code gramsPerUnit} can also
 * be removed. The kind and the stock unit never change.
 */
public record ItemChanges(
        String name,
        UUID categoryId,
        BigDecimal minimumStock,
        Change<BigDecimal> gramsPerUnit,
        Boolean active) {

    public ItemChanges {
        if (gramsPerUnit == null) {
            gramsPerUnit = Change.keep();
        }
    }

    public boolean isEmpty() {
        return name == null
                && categoryId == null
                && minimumStock == null
                && gramsPerUnit instanceof Change.Keep
                && active == null;
    }
}
