package ec.com.erre.inventory.infrastructure.in.rest;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import ec.com.erre.inventory.application.UnitChanges;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code PATCH /api/units/{code}}, a JSON Merge Patch (contract: {@code UnitPatch}). A
 * missing field does not change; an explicit null is rejected, because neither field can be
 * removed. Any other field (code, dimension, baseFactor) is rejected as unknown.
 */
record UnitPatchRequest(
        @JsonSetter(nulls = Nulls.FAIL)
                @Size(min = 1, max = ApiPatterns.NAME_MAX, message = "{unit.name.invalid}")
                @Pattern(regexp = ".*\\S.*", message = "{unit.name.invalid}")
                String name,
        @JsonSetter(nulls = Nulls.FAIL) Boolean active) {

    boolean isEmpty() {
        return name == null && active == null;
    }

    UnitChanges toChanges() {
        return new UnitChanges(name == null ? null : name.strip(), active);
    }
}
