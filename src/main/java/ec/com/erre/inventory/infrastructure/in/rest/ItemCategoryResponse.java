package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.domain.ItemCategory;
import java.util.UUID;

/** A category as the API returns it (contract: {@code ItemCategory}). */
record ItemCategoryResponse(UUID id, String name, boolean active) {

    static ItemCategoryResponse from(ItemCategory category) {
        return new ItemCategoryResponse(category.id(), category.name(), category.active());
    }
}
