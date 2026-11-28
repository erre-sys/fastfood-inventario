package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.port.UnitSearch;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Translates a unit search into JPA criteria. Only the filters present are applied. */
final class UnitSpecifications {

    private static final char ESCAPE = '\\';

    private UnitSpecifications() {}

    static Specification<UnitEntity> matching(UnitSearch search) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (search.dimension() != null) {
                predicates.add(cb.equal(root.get("dimension"), search.dimension()));
            }
            if (search.active() != null) {
                predicates.add(cb.equal(root.get("active"), search.active()));
            }
            if (search.text() != null) {
                var pattern = "%" + escapeLike(search.text().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("code")), pattern, ESCAPE),
                                cb.like(cb.lower(root.get("name")), pattern, ESCAPE)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /** The text is searched literally: % and _ typed by a person are not wildcards. */
    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
