package ec.com.erre.inventory.infrastructure.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

interface ItemCategoryJpaRepository extends JpaRepository<ItemCategoryEntity, UUID> {

    List<ItemCategoryEntity> findByActive(boolean active, Sort sort);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
