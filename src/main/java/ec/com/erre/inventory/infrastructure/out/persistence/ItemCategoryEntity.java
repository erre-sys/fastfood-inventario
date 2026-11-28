package ec.com.erre.inventory.infrastructure.out.persistence;

import ec.com.erre.inventory.domain.Audit;
import ec.com.erre.inventory.domain.ItemCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

/** Row of the item_category table. */
@Entity
@Table(name = "item_category")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
class ItemCategoryEntity {

    @Id private UUID id;

    @Column(nullable = false, length = 60)
    private String name;

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

    static ItemCategoryEntity from(ItemCategory category) {
        var entity = new ItemCategoryEntity();
        entity.setId(category.id());
        entity.update(category);
        return entity;
    }

    /** Copies what may change: the name and the state. */
    void update(ItemCategory category) {
        setName(category.name());
        setActive(category.active());
    }

    ItemCategory toDomain() {
        return new ItemCategory(
                id, name, active, new Audit(createdBy, createdAt, updatedBy, updatedAt));
    }
}
