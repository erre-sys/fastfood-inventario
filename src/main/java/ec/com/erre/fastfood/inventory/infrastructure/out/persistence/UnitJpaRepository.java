package ec.com.erre.fastfood.inventory.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface UnitJpaRepository extends JpaRepository<UnitEntity, String> {}
