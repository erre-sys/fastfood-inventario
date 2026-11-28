package ec.com.erre.inventory.application;

import ec.com.erre.inventory.domain.Quantity;

/** A quantity and its equivalent in another unit of the same dimension. */
public record Conversion(Quantity source, Quantity target) {}
