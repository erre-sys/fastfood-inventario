package ec.com.erre.inventory.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface UnitJpaRepository
        extends JpaRepository<UnitEntity, String>, JpaSpecificationExecutor<UnitEntity> {}
