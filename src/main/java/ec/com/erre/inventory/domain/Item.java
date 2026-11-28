package ec.com.erre.inventory.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Something that is stocked: flour, eggs, a batch of focaccia. The kind and the stock unit never
 * change, because the kardex is kept in that unit.
 *
 * @param category the category it belongs to, with its name for display
 * @param stockUnit code of the unit the stock is counted in (g, ml, unit)
 * @param dimension dimension of the stock unit
 * @param minimumStock below this, the stock is low; in the stock unit, up to 3 decimals
 * @param gramsPerUnit only for items counted in units: what one weighs (an egg: 60 g); else null
 * @param yieldPercent usable part of what is bought (peeled onion: 91); 100 without measurements
 * @param audit who created and changed it; null for an item that has not been stored yet
 */
public record Item(
        UUID id,
        String name,
        ItemKind kind,
        CategoryRef category,
        String stockUnit,
        Dimension dimension,
        BigDecimal minimumStock,
        BigDecimal gramsPerUnit,
        BigDecimal yieldPercent,
        boolean active,
        Audit audit) {

    public static final int NAME_MAX = 80;
    static final BigDecimal FULL_YIELD = new BigDecimal("100");

    /** The category of an item: its id, and its name for display. */
    public record CategoryRef(UUID id, String name) {

        public static CategoryRef of(ItemCategory category) {
            return new CategoryRef(category.id(), category.name());
        }
    }

    public Item {
        if (id == null) {
            throw new IllegalArgumentException("Item id is required");
        }
        if (name == null || name.isBlank() || name.length() > NAME_MAX) {
            throw new IllegalArgumentException(
                    "Item name is required, up to " + NAME_MAX + " characters");
        }
        if (kind == null || category == null || stockUnit == null || dimension == null) {
            throw new IllegalArgumentException("Kind, category and stock unit are required");
        }
        if (minimumStock == null || minimumStock.signum() < 0 || !Quantities.fits(minimumStock)) {
            throw new IllegalArgumentException(
                    "Minimum stock must be zero or more, up to 3 decimal places");
        }
        if (gramsPerUnit != null) {
            if (dimension != Dimension.COUNT) {
                throw new IllegalArgumentException(
                        "Grams per unit only applies to items counted in units");
            }
            if (gramsPerUnit.signum() <= 0 || !Quantities.fits(gramsPerUnit)) {
                throw new IllegalArgumentException(
                        "Grams per unit must be positive, up to 3 decimal places");
            }
        }
        if (yieldPercent == null
                || yieldPercent.signum() <= 0
                || yieldPercent.compareTo(FULL_YIELD) > 0
                || yieldPercent.stripTrailingZeros().scale() > Yield.PERCENT_SCALE) {
            throw new IllegalArgumentException("Yield must be above 0 and up to 100");
        }
    }

    /** A new, active item with full yield, counted in the given unit. */
    public static Item create(
            UUID id,
            String name,
            ItemKind kind,
            ItemCategory category,
            Unit stockUnit,
            BigDecimal minimumStock,
            BigDecimal gramsPerUnit) {
        return new Item(
                id,
                name,
                kind,
                CategoryRef.of(category),
                stockUnit.code(),
                stockUnit.dimension(),
                minimumStock,
                gramsPerUnit,
                FULL_YIELD,
                true,
                null);
    }

    public Item withName(String newName) {
        return new Item(
                id,
                newName,
                kind,
                category,
                stockUnit,
                dimension,
                minimumStock,
                gramsPerUnit,
                yieldPercent,
                active,
                audit);
    }

    public Item withCategory(ItemCategory newCategory) {
        return new Item(
                id,
                name,
                kind,
                CategoryRef.of(newCategory),
                stockUnit,
                dimension,
                minimumStock,
                gramsPerUnit,
                yieldPercent,
                active,
                audit);
    }

    public Item withMinimumStock(BigDecimal newMinimumStock) {
        return new Item(
                id,
                name,
                kind,
                category,
                stockUnit,
                dimension,
                newMinimumStock,
                gramsPerUnit,
                yieldPercent,
                active,
                audit);
    }

    /** Sets or, with null, removes the weight of one unit. */
    public Item withGramsPerUnit(BigDecimal newGramsPerUnit) {
        return new Item(
                id,
                name,
                kind,
                category,
                stockUnit,
                dimension,
                minimumStock,
                newGramsPerUnit,
                yieldPercent,
                active,
                audit);
    }

    /** The yield of a new measurement, which becomes the current one. */
    public Item withYield(Yield measured) {
        if (!measured.itemId().equals(id)) {
            throw new IllegalArgumentException("The yield belongs to another item");
        }
        return new Item(
                id,
                name,
                kind,
                category,
                stockUnit,
                dimension,
                minimumStock,
                gramsPerUnit,
                measured.yieldPercent(),
                active,
                audit);
    }

    public Item withActive(boolean newActive) {
        return new Item(
                id,
                name,
                kind,
                category,
                stockUnit,
                dimension,
                minimumStock,
                gramsPerUnit,
                yieldPercent,
                newActive,
                audit);
    }
}
