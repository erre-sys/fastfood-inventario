package ec.com.erre.inventory.domain.port;

import java.util.List;

/** One page of results and the totals needed to page through the rest. */
public record ResultPage<T>(List<T> content, int page, int size, long totalElements) {

    public ResultPage {
        content = List.copyOf(content);
    }

    public int totalPages() {
        return (int) ((totalElements + size - 1) / size);
    }
}
