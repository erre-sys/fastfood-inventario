package ec.com.erre.inventory.application;

import java.math.BigDecimal;

/** A yield measurement: what was weighed and the usable part, in the stock unit of the item. */
public record NewYield(BigDecimal grossQuantity, BigDecimal usableQuantity, String note) {}
