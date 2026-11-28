package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.Item;
import ec.com.erre.inventory.domain.ItemKind;
import java.time.Instant;
import java.util.UUID;

/** An item as the API returns it (contract: {@code Item}). */
record ItemResponse(
        UUID id,
        String name,
        ItemKind kind,
        CategoryRef category,
        String stockUnit,
        Dimension dimension,
        String minimumStock,
        String gramsPerUnit,
        String yieldPercent,
        boolean active,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt) {

    record CategoryRef(UUID id, String name) {}

    static ItemResponse from(Item item) {
        var audit = item.audit();
        return new ItemResponse(
                item.id(),
                item.name(),
                item.kind(),
                new CategoryRef(item.category().id(), item.category().name()),
                item.stockUnit(),
                item.dimension(),
                Decimals.compact(item.minimumStock()),
                item.gramsPerUnit() == null ? null : Decimals.compact(item.gramsPerUnit()),
                Decimals.compact(item.yieldPercent()),
                item.active(),
                audit == null ? null : audit.createdBy(),
                audit == null ? null : audit.createdAt(),
                audit == null ? null : audit.updatedBy(),
                audit == null ? null : audit.updatedAt());
    }
}
