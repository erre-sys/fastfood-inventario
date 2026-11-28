package ec.com.erre.fastfood.inventory.infrastructure.out.persistence;

import ec.com.erre.fastfood.inventory.domain.Dimension;
import ec.com.erre.fastfood.inventory.domain.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Row of the unit table. Lives only in the infrastructure; the domain uses Unit. */
@Entity
@Table(name = "unit")
@Getter
@Setter
@NoArgsConstructor
class UnitEntity {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Dimension dimension;

    @Column(name = "base_factor", nullable = false, precision = 18, scale = 6)
    private BigDecimal baseFactor;

    @Column(nullable = false)
    private boolean standard;

    // Filled in by the database (DEFAULT). Auditing with the real user comes with the API.
    @Column(
            name = "created_by",
            nullable = false,
            length = 100,
            insertable = false,
            updatable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    static UnitEntity from(Unit unit) {
        var entity = new UnitEntity();
        entity.setCode(unit.code());
        entity.setName(unit.name());
        entity.setDimension(unit.dimension());
        entity.setBaseFactor(unit.baseFactor());
        return entity;
    }

    Unit toDomain() {
        return new Unit(code, name, dimension, baseFactor);
    }
}
