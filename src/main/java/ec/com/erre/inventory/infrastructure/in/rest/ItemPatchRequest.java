package ec.com.erre.inventory.infrastructure.in.rest;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import ec.com.erre.inventory.application.Change;
import ec.com.erre.inventory.application.ItemChanges;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Body of {@code PATCH /items/{id}}, a JSON Merge Patch (contract: {@code ItemPatch}). A missing
 * field does not change. An explicit null removes {@code gramsPerUnit} and is rejected for the
 * other fields. The kind and the stock unit are rejected as unknown fields.
 */
class ItemPatchRequest {

    @Size(min = 1, max = ApiPatterns.ITEM_NAME_MAX, message = "{item.name.invalid}")
    @Pattern(regexp = ".*\\S.*", message = "{item.name.invalid}")
    private String name;

    private UUID categoryId;

    @Pattern(regexp = ApiPatterns.QUANTITY, message = "{quantity.value.invalid}")
    private String minimumStock;

    @Pattern(regexp = ApiPatterns.GRAMS_PER_UNIT, message = "{item.gramsPerUnit.invalid}")
    @DecimalMin(value = "0", inclusive = false, message = "{item.gramsPerUnit.invalid}")
    private String gramsPerUnit;

    private boolean gramsPerUnitPresent;

    private Boolean active;

    String getName() {
        return name;
    }

    @JsonSetter(nulls = Nulls.FAIL)
    void setName(String name) {
        this.name = name;
    }

    UUID getCategoryId() {
        return categoryId;
    }

    @JsonSetter(nulls = Nulls.FAIL)
    void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    String getMinimumStock() {
        return minimumStock;
    }

    @JsonSetter(nulls = Nulls.FAIL)
    void setMinimumStock(String minimumStock) {
        this.minimumStock = minimumStock;
    }

    String getGramsPerUnit() {
        return gramsPerUnit;
    }

    /** Called only when the field is in the body; null means remove it. */
    @JsonSetter
    void setGramsPerUnit(String gramsPerUnit) {
        this.gramsPerUnit = gramsPerUnit;
        this.gramsPerUnitPresent = true;
    }

    Boolean getActive() {
        return active;
    }

    @JsonSetter(nulls = Nulls.FAIL)
    void setActive(Boolean active) {
        this.active = active;
    }

    boolean isEmpty() {
        return name == null
                && categoryId == null
                && minimumStock == null
                && !gramsPerUnitPresent
                && active == null;
    }

    ItemChanges toChanges() {
        Change<BigDecimal> grams;
        if (!gramsPerUnitPresent) {
            grams = Change.keep();
        } else if (gramsPerUnit == null) {
            grams = Change.remove();
        } else {
            grams = Change.to(new BigDecimal(gramsPerUnit));
        }
        return new ItemChanges(
                name == null ? null : name.strip(),
                categoryId,
                minimumStock == null ? null : new BigDecimal(minimumStock),
                grams,
                active);
    }
}
