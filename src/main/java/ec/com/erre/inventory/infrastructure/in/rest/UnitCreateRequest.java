package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.application.NewUnit;
import ec.com.erre.inventory.domain.Dimension;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Body of {@code POST /api/units} (contract: {@code UnitCreate}). Unknown fields are rejected. */
record UnitCreateRequest(
        @NotNull(message = "{field.required}")
                @Pattern(regexp = ApiPatterns.UNIT_CODE, message = "{unit.code.invalid}")
                String code,
        @NotBlank(message = "{field.required}")
                @Size(max = ApiPatterns.NAME_MAX, message = "{unit.name.invalid}")
                String name,
        @NotNull(message = "{field.required}") Dimension dimension,
        @NotNull(message = "{field.required}")
                @Pattern(regexp = ApiPatterns.BASE_FACTOR, message = "{unit.baseFactor.invalid}")
                @DecimalMin(value = "0", inclusive = false, message = "{unit.baseFactor.invalid}")
                String baseFactor) {

    NewUnit toNewUnit() {
        return new NewUnit(code, name.strip(), dimension, new BigDecimal(baseFactor));
    }
}
