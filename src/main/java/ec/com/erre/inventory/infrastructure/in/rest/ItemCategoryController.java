package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.application.ItemCategoryService;
import ec.com.erre.inventory.infrastructure.security.Permissions;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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

/** Item categories: {@code /item-categories}. Each operation checks its permission. */
@RestController
@RequestMapping("/item-categories")
public class ItemCategoryController {

    private static final String CAN_READ = "hasAuthority('" + Permissions.ITEMS_READ + "')";
    private static final String CAN_CREATE = "hasAuthority('" + Permissions.ITEMS_CREATE + "')";
    private static final String CAN_EDIT = "hasAuthority('" + Permissions.ITEMS_EDIT + "')";

    private final ItemCategoryService categories;

    public ItemCategoryController(ItemCategoryService categories) {
        this.categories = categories;
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    public List<ItemCategoryResponse> list(
            @RequestParam(name = "active", required = false) Boolean active) {
        return categories.list(active).stream().map(ItemCategoryResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize(CAN_CREATE)
    public ResponseEntity<ItemCategoryResponse> create(
            @Valid @RequestBody ItemCategoryCreateRequest request) {
        var created = categories.create(request.name());
        var location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.id())
                        .toUri();
        return ResponseEntity.created(location).body(ItemCategoryResponse.from(created));
    }

    @PatchMapping(path = "/{id}", consumes = UnitController.MERGE_PATCH_JSON)
    @PreAuthorize(CAN_EDIT)
    public ItemCategoryResponse update(
            @PathVariable("id") UUID id, @Valid @RequestBody ItemCategoryPatchRequest request) {
        if (request.isEmpty()) {
            throw new EmptyPatchException();
        }
        return ItemCategoryResponse.from(categories.update(id, request.toChanges()));
    }
}
