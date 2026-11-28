package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.Unit;
import java.time.Instant;

/** A unit as the API returns it (contract: {@code Unit}). */
record UnitResponse(
        String code,
        String name,
        Dimension dimension,
        String baseFactor,
        boolean standard,
        boolean active,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt) {

    static UnitResponse from(Unit unit) {
        var audit = unit.audit();
        return new UnitResponse(
                unit.code(),
                unit.name(),
                unit.dimension(),
                Decimals.compact(unit.baseFactor()),
                unit.standard(),
                unit.active(),
                audit == null ? null : audit.createdBy(),
                audit == null ? null : audit.createdAt(),
                audit == null ? null : audit.updatedBy(),
                audit == null ? null : audit.updatedAt());
    }
}
