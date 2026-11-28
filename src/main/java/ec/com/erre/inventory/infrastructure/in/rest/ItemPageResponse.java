package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.domain.Item;
import ec.com.erre.inventory.domain.port.ResultPage;
import ec.com.erre.inventory.infrastructure.in.rest.UnitPageResponse.PageMetadata;
import java.util.List;

/** One page of items (contract: {@code ItemPage}). */
record ItemPageResponse(List<ItemResponse> content, PageMetadata page) {

    static ItemPageResponse from(ResultPage<Item> result) {
        return new ItemPageResponse(
                result.content().stream().map(ItemResponse::from).toList(),
                new PageMetadata(
                        result.size(), result.page(), result.totalElements(), result.totalPages()));
    }
}
