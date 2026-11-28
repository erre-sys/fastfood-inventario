package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.Audit;
import ec.com.erre.inventory.domain.Dimension;
import ec.com.erre.inventory.domain.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Row of the unit table. Lives only in the infrastructure; the domain uses Unit. */
@Entity
@Table(name = "unit")
@EntityListeners(AuditingEntityListener.class)
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
    @Column(nullable = false, length = 10, updatable = false)
    private Dimension dimension;

    @Column(name = "base_factor", nullable = false, precision = 18, scale = 6, updatable = false)
    private BigDecimal baseFactor;

    @Column(nullable = false, updatable = false)
    private boolean standard;

    @Column(nullable = false)
    private boolean active;

    // Who and when, filled in by Spring Data auditing from the user in the token (ADR 0029: UTC).
    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    static UnitEntity from(Unit unit) {
        var entity = new UnitEntity();
        entity.setCode(unit.code());
        entity.setName(unit.name());
        entity.setDimension(unit.dimension());
        entity.setBaseFactor(unit.baseFactor());
        entity.setStandard(unit.standard());
        entity.setActive(unit.active());
        return entity;
    }

    /** Copies what may change after creation: the name and the state. Nothing else. */
    void update(Unit unit) {
        setName(unit.name());
        setActive(unit.active());
    }

    Unit toDomain() {
        return new Unit(
                code,
                name,
                dimension,
                baseFactor,
                standard,
                active,
                new Audit(createdBy, createdAt, updatedBy, updatedAt));
    }
}
