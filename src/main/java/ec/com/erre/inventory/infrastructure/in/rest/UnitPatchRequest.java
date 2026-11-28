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
 *
 * <p>A class with setters, not a record: Jackson only calls the setter of a field that is present,
 * so a missing field and an explicit null can be told apart. With a record both arrive as null.
 */
class UnitPatchRequest {

    @Size(min = 1, max = ApiPatterns.NAME_MAX, message = "{unit.name.invalid}")
    @Pattern(regexp = ".*\\S.*", message = "{unit.name.invalid}")
    private String name;

    private Boolean active;

    String getName() {
        return name;
    }

    @JsonSetter(nulls = Nulls.FAIL)
    void setName(String name) {
        this.name = name;
    }

    Boolean getActive() {
        return active;
    }

    @JsonSetter(nulls = Nulls.FAIL)
    void setActive(Boolean active) {
        this.active = active;
    }

    boolean isEmpty() {
        return name == null && active == null;
    }

    UnitChanges toChanges() {
        return new UnitChanges(name == null ? null : name.strip(), active);
    }
}
