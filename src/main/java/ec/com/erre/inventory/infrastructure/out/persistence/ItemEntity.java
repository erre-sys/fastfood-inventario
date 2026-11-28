package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.Audit;
import ec.com.erre.inventory.domain.Item;
import ec.com.erre.inventory.domain.ItemKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Row of the item table. The kind and the stock unit are written once. */
@Entity
@Table(name = "item")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
class ItemEntity {

    @Id private UUID id;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private ItemKind kind;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ItemCategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_unit", nullable = false, updatable = false)
    private UnitEntity stockUnit;

    @Column(name = "minimum_stock", nullable = false, precision = 15, scale = 3)
    private BigDecimal minimumStock;

    @Column(name = "grams_per_unit", precision = 15, scale = 3)
    private BigDecimal gramsPerUnit;

    @Column(name = "yield_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal yieldPercent;

    @Column(nullable = false)
    private boolean active;

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

    static ItemEntity from(Item item, ItemCategoryEntity category, UnitEntity stockUnit) {
        var entity = new ItemEntity();
        entity.setId(item.id());
        entity.setKind(item.kind());
        entity.setStockUnit(stockUnit);
        entity.update(item, category);
        return entity;
    }

    /** Copies what may change. The kind and the stock unit are never copied. */
    void update(Item item, ItemCategoryEntity newCategory) {
        setName(item.name());
        setCategory(newCategory);
        setMinimumStock(item.minimumStock());
        setGramsPerUnit(item.gramsPerUnit());
        setYieldPercent(item.yieldPercent());
        setActive(item.active());
    }

    Item toDomain() {
        return new Item(
                id,
                name,
                kind,
                new Item.CategoryRef(category.getId(), category.getName()),
                stockUnit.getCode(),
                stockUnit.getDimension(),
                minimumStock,
                gramsPerUnit,
                yieldPercent,
                active,
                new Audit(createdBy, createdAt, updatedBy, updatedAt));
    }
}
