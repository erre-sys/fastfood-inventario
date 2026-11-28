package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.Audit;
import ec.com.erre.inventory.domain.Yield;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Row of the item_yield table. Inserted once, never updated. */
@Entity
@Table(name = "item_yield")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
class YieldEntity {

    @Id private UUID id;

    @Column(name = "item_id", nullable = false, updatable = false)
    private UUID itemId;

    @Column(name = "gross_quantity", nullable = false, precision = 15, scale = 3, updatable = false)
    private BigDecimal grossQuantity;

    @Column(
            name = "usable_quantity",
            nullable = false,
            precision = 15,
            scale = 3,
            updatable = false)
    private BigDecimal usableQuantity;

    @Column(name = "yield_percent", nullable = false, precision = 5, scale = 2, updatable = false)
    private BigDecimal yieldPercent;

    @Column(length = 200, updatable = false)
    private String note;

    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    static YieldEntity from(Yield measured) {
        var entity = new YieldEntity();
        entity.setId(measured.id());
        entity.setItemId(measured.itemId());
        entity.setGrossQuantity(measured.grossQuantity());
        entity.setUsableQuantity(measured.usableQuantity());
        entity.setYieldPercent(measured.yieldPercent());
        entity.setNote(measured.note());
        return entity;
    }

    Yield toDomain() {
        return new Yield(
                id,
                itemId,
                grossQuantity,
                usableQuantity,
                yieldPercent,
                note,
                new Audit(createdBy, createdAt, null, null));
    }
}
