package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.application.UnitService;
import ec.com.erre.inventory.infrastructure.security.Permissions;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Conversions between units: {@code /api/conversions}. A resource of its own, so no unit code can
 * collide with it.
 */
@RestController
public class ConversionController {

    private final UnitService units;

    public ConversionController(UnitService units) {
        this.units = units;
    }

    @GetMapping("/api/conversions")
    @PreAuthorize("hasAuthority('" + Permissions.UNITS_READ + "')")
    public ConversionResponse convert(
            @RequestParam("from")
                    @Pattern(regexp = ApiPatterns.UNIT_CODE, message = "{unit.code.invalid}")
                    String from,
            @RequestParam("to")
                    @Pattern(regexp = ApiPatterns.UNIT_CODE, message = "{unit.code.invalid}")
                    String to,
            @RequestParam("value")
                    @Pattern(regexp = ApiPatterns.QUANTITY, message = "{quantity.value.invalid}")
                    String value) {
        return ConversionResponse.from(units.convert(from, to, new BigDecimal(value)));
    }
}
