package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.domain.Yield;
import java.time.Instant;
import java.util.UUID;

/** A yield measurement as the API returns it (contract: {@code Yield}). */
record YieldResponse(
        UUID id,
        String grossQuantity,
        String usableQuantity,
        String yieldPercent,
        String wastePercent,
        String note,
        String createdBy,
        Instant createdAt) {

    static YieldResponse from(Yield measured) {
        var audit = measured.audit();
        return new YieldResponse(
                measured.id(),
                Decimals.compact(measured.grossQuantity()),
                Decimals.compact(measured.usableQuantity()),
                Decimals.compact(measured.yieldPercent()),
                Decimals.compact(measured.wastePercent()),
                measured.note(),
                audit == null ? null : audit.createdBy(),
                audit == null ? null : audit.createdAt());
    }
}
