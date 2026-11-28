package ec.com.erre.inventory.infrastructure.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface YieldJpaRepository extends JpaRepository<YieldEntity, UUID> {

    List<YieldEntity> findByItemIdOrderByCreatedAtDescIdDesc(UUID itemId);
}
