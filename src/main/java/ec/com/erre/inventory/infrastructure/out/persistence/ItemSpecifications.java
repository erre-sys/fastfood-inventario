package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.port.ItemSearch;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Translates an item search into JPA criteria. Only the filters present are applied. */
final class ItemSpecifications {

    private static final char ESCAPE = '\\';

    private ItemSpecifications() {}

    static Specification<ItemEntity> matching(ItemSearch search) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (search.kind() != null) {
                predicates.add(cb.equal(root.get("kind"), search.kind()));
            }
            if (search.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), search.categoryId()));
            }
            if (search.active() != null) {
                predicates.add(cb.equal(root.get("active"), search.active()));
            }
            if (search.text() != null) {
                var pattern =
                        "%"
                                + UnitSpecifications.escapeLike(
                                        search.text().toLowerCase(Locale.ROOT))
                                + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern, ESCAPE));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
