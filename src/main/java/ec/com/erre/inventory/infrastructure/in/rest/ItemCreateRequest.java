package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.application.NewItem;
import ec.com.erre.inventory.domain.ItemKind;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

/** Body of {@code POST /items} (contract: {@code ItemCreate}). Unknown fields are rejected. */
record ItemCreateRequest(
        @NotBlank(message = "{field.required}")
                @Size(max = ApiPatterns.ITEM_NAME_MAX, message = "{item.name.invalid}")
                String name,
        @NotNull(message = "{field.required}") ItemKind kind,
        @NotNull(message = "{field.required}") UUID categoryId,
        @NotNull(message = "{field.required}")
                @Pattern(regexp = ApiPatterns.UNIT_CODE, message = "{unit.code.invalid}")
                String stockUnit,
        @NotNull(message = "{field.required}")
                @Pattern(regexp = ApiPatterns.QUANTITY, message = "{quantity.value.invalid}")
                String minimumStock,
        @Pattern(regexp = ApiPatterns.GRAMS_PER_UNIT, message = "{item.gramsPerUnit.invalid}")
                @DecimalMin(value = "0", inclusive = false, message = "{item.gramsPerUnit.invalid}")
                String gramsPerUnit) {

    NewItem toNewItem() {
        return new NewItem(
                name,
                kind,
                categoryId,
                stockUnit,
                new BigDecimal(minimumStock),
                gramsPerUnit == null ? null : new BigDecimal(gramsPerUnit));
    }
}
