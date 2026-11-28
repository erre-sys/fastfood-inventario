package ec.com.erre.inventory.application;

import ec.com.erre.inventory.domain.ItemKind;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data to create an item.
 *
 * @param stockUnit code of the unit the stock is counted in; must be active
 * @param gramsPerUnit optional; only for a stock unit counted in units
 */
public record NewItem(
        String name,
        ItemKind kind,
        UUID categoryId,
        String stockUnit,
        BigDecimal minimumStock,
        BigDecimal gramsPerUnit) {}
