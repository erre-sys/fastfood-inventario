package ec.com.erre.inventory.infrastructure.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Category and stock unit are loaded with the item (one query, not one per row). */
interface ItemJpaRepository
        extends JpaRepository<ItemEntity, UUID>, JpaSpecificationExecutor<ItemEntity> {

    @Override
    @EntityGraph(attributePaths = {"category", "stockUnit"})
    Optional<ItemEntity> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"category", "stockUnit"})
    Page<ItemEntity> findAll(Specification<ItemEntity> spec, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
