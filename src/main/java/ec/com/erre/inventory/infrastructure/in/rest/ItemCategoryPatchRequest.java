package ec.com.erre.inventory.infrastructure.in.rest;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import ec.com.erre.inventory.application.CategoryChanges;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code PATCH /item-categories/{id}}, a JSON Merge Patch (contract: {@code
 * ItemCategoryPatch}). A class with setters, so a missing field and an explicit null differ (see
 * {@link UnitPatchRequest}).
 */
class ItemCategoryPatchRequest {

    @Size(min = 1, max = ApiPatterns.NAME_MAX, message = "{category.name.invalid}")
    @Pattern(regexp = ".*\\S.*", message = "{category.name.invalid}")
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

    CategoryChanges toChanges() {
        return new CategoryChanges(name, active);
    }
}
