package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.application.NewYield;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Body of {@code POST /items/{id}/yields} (contract: {@code YieldCreate}). */
record YieldCreateRequest(
        @NotNull(message = "{field.required}")
                @Pattern(regexp = ApiPatterns.QUANTITY, message = "{yield.quantity.invalid}")
                @DecimalMin(value = "0", inclusive = false, message = "{yield.quantity.invalid}")
                String grossQuantity,
        @NotNull(message = "{field.required}")
                @Pattern(regexp = ApiPatterns.QUANTITY, message = "{yield.quantity.invalid}")
                @DecimalMin(value = "0", inclusive = false, message = "{yield.quantity.invalid}")
                String usableQuantity,
        @Size(max = ApiPatterns.NOTE_MAX, message = "{yield.note.invalid}") String note) {

    NewYield toNewYield() {
        return new NewYield(new BigDecimal(grossQuantity), new BigDecimal(usableQuantity), note);
    }
}
