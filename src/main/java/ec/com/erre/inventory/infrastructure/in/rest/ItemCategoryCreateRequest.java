package ec.com.erre.inventory.infrastructure.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of {@code POST /item-categories} (contract: {@code ItemCategoryCreate}). */
record ItemCategoryCreateRequest(
        @NotBlank(message = "{field.required}")
                @Size(max = ApiPatterns.NAME_MAX, message = "{category.name.invalid}")
                String name) {}
