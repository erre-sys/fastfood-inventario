package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.application.UnitService;
import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.port.UnitSearch;
import ec.com.erre.inventory.domain.port.UnitSort;
import ec.com.erre.inventory.infrastructure.security.Permissions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Units of measure: {@code /units}, public as {@code /api/v1/inventario/units} (the Ingress strips
 * the prefix). Each operation checks its permission.
 */
@RestController
@RequestMapping("/units")
public class UnitController {

    static final String MERGE_PATCH_JSON = "application/merge-patch+json";

    private static final String CAN_READ = "hasAuthority('" + Permissions.UNITS_READ + "')";
    private static final String CAN_CREATE = "hasAuthority('" + Permissions.UNITS_CREATE + "')";
    private static final String CAN_EDIT = "hasAuthority('" + Permissions.UNITS_EDIT + "')";

    private final UnitService units;

    public UnitController(UnitService units) {
        this.units = units;
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    public UnitPageResponse list(
            @RequestParam(name = "page", defaultValue = "0")
                    @Min(value = 0, message = "{page.invalid}")
                    int page,
            @RequestParam(name = "size", defaultValue = "" + UnitSearch.DEFAULT_SIZE)
                    @Min(value = 1, message = "{size.invalid}")
                    @Max(value = UnitSearch.MAX_SIZE, message = "{size.invalid}")
                    int size,
            @RequestParam(name = "sort", defaultValue = "code,asc")
                    @Pattern(regexp = ApiPatterns.UNIT_SORT, message = "{sort.invalid}")
                    String sort,
            @RequestParam(name = "dimension", required = false) Dimension dimension,
            @RequestParam(name = "text", required = false)
                    @Size(min = 1, max = ApiPatterns.NAME_MAX, message = "{text.invalid}")
                    String text,
            @RequestParam(name = "active", required = false) Boolean active) {
        var parts = sort.split(",");
        var field = UnitSort.valueOf(parts[0].toUpperCase(Locale.ROOT));
        var ascending = parts.length == 1 || "asc".equals(parts[1]);
        var search = new UnitSearch(dimension, text, active, page, size, field, ascending);
        return UnitPageResponse.from(units.list(search));
    }

    @GetMapping("/{code}")
    @PreAuthorize(CAN_READ)
    public UnitResponse get(@PathVariable("code") String code) {
        return UnitResponse.from(units.get(code));
    }

    @PostMapping
    @PreAuthorize(CAN_CREATE)
    public ResponseEntity<UnitResponse> create(@Valid @RequestBody UnitCreateRequest request) {
        var created = units.create(request.toNewUnit());
        var location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{code}")
                        .buildAndExpand(created.code())
                        .toUri();
        return ResponseEntity.created(location).body(UnitResponse.from(created));
    }

    @PatchMapping(path = "/{code}", consumes = MERGE_PATCH_JSON)
    @PreAuthorize(CAN_EDIT)
    public UnitResponse update(
            @PathVariable("code") String code, @Valid @RequestBody UnitPatchRequest request) {
        if (request.isEmpty()) {
            throw new EmptyPatchException();
        }
        return UnitResponse.from(units.update(code, request.toChanges()));
    }
}
