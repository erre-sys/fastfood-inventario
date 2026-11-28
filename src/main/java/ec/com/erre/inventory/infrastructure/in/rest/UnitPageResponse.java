package ec.com.erre.inventory.infrastructure.in.rest;

import ec.com.erre.inventory.domain.Unit;
import ec.com.erre.inventory.domain.port.ResultPage;
import java.util.List;

/**
 * One page of units (contract: {@code UnitPage}), the same shape as Spring Data's {@code
 * PagedModel}, so clients can share the paging code.
 */
record UnitPageResponse(List<UnitResponse> content, PageMetadata page) {

    record PageMetadata(int size, int number, long totalElements, int totalPages) {}

    static UnitPageResponse from(ResultPage<Unit> result) {
        return new UnitPageResponse(
                result.content().stream().map(UnitResponse::from).toList(),
                new PageMetadata(
                        result.size(), result.page(), result.totalElements(), result.totalPages()));
    }
}
