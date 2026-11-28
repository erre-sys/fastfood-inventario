package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.application.ItemService;
import ec.com.erre.inventory.domain.ItemKind;
import ec.com.erre.inventory.domain.port.ItemSearch;
import ec.com.erre.inventory.domain.port.ItemSort;
import ec.com.erre.inventory.domain.port.UnitSearch;
import ec.com.erre.inventory.infrastructure.security.Permissions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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

/** Items and their yields: {@code /items}. Each operation checks its permission. */
@RestController
@RequestMapping("/items")
public class ItemController {

    private static final String CAN_READ = "hasAuthority('" + Permissions.ITEMS_READ + "')";
    private static final String CAN_CREATE = "hasAuthority('" + Permissions.ITEMS_CREATE + "')";
    private static final String CAN_EDIT = "hasAuthority('" + Permissions.ITEMS_EDIT + "')";

    private final ItemService items;

    public ItemController(ItemService items) {
        this.items = items;
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    public ItemPageResponse list(
            @RequestParam(name = "page", defaultValue = "0")
                    @Min(value = 0, message = "{page.invalid}")
                    int page,
            @RequestParam(name = "size", defaultValue = "" + UnitSearch.DEFAULT_SIZE)
                    @Min(value = 1, message = "{size.invalid}")
                    @Max(value = UnitSearch.MAX_SIZE, message = "{size.invalid}")
                    int size,
            @RequestParam(name = "sort", defaultValue = "name,asc")
                    @Pattern(regexp = ApiPatterns.ITEM_SORT, message = "{item.sort.invalid}")
                    String sort,
            @RequestParam(name = "text", required = false)
                    @Size(min = 1, max = ApiPatterns.ITEM_NAME_MAX, message = "{item.text.invalid}")
                    String text,
            @RequestParam(name = "kind", required = false) ItemKind kind,
            @RequestParam(name = "category", required = false) UUID category,
            @RequestParam(name = "active", required = false) Boolean active) {
        var parts = sort.split(",");
        var field = ItemSort.valueOf(parts[0].toUpperCase(Locale.ROOT));
        var ascending = parts.length == 1 || "asc".equals(parts[1]);
        var search = new ItemSearch(text, kind, category, active, page, size, field, ascending);
        return ItemPageResponse.from(items.list(search));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ)
    public ItemResponse get(@PathVariable("id") UUID id) {
        return ItemResponse.from(items.get(id));
    }

    @PostMapping
    @PreAuthorize(CAN_CREATE)
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemCreateRequest request) {
        var created = items.create(request.toNewItem());
        var location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.id())
                        .toUri();
        return ResponseEntity.created(location).body(ItemResponse.from(created));
    }

    @PatchMapping(path = "/{id}", consumes = UnitController.MERGE_PATCH_JSON)
    @PreAuthorize(CAN_EDIT)
    public ItemResponse update(
            @PathVariable("id") UUID id, @Valid @RequestBody ItemPatchRequest request) {
        if (request.isEmpty()) {
            throw new EmptyPatchException();
        }
        return ItemResponse.from(items.update(id, request.toChanges()));
    }

    @GetMapping("/{id}/yields")
    @PreAuthorize(CAN_READ)
    public List<YieldResponse> yields(@PathVariable("id") UUID id) {
        return items.yields(id).stream().map(YieldResponse::from).toList();
    }

    @PostMapping("/{id}/yields")
    @PreAuthorize(CAN_EDIT)
    public ResponseEntity<YieldResponse> recordYield(
            @PathVariable("id") UUID id, @Valid @RequestBody YieldCreateRequest request) {
        var measured = items.recordYield(id, request.toNewYield());
        return ResponseEntity.status(HttpStatus.CREATED).body(YieldResponse.from(measured));
    }
}
