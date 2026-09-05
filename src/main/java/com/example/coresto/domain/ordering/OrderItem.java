package com.example.coresto.domain.ordering;

import com.example.coresto.domain.catalog.Product;
import com.example.coresto.domain.catalog.ProductVariant;
import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.Money;
import com.example.coresto.domain.common.OrderItemStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Catalog price edits must NOT mutate OrderItem snapshots.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"modifiers", "removedIngredients"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class OrderItem extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "variant_name")
    private String variantName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount", nullable = false, precision = 12, scale = 3)),
        @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency", nullable = false))
    })
    private Money unitPrice;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "line_total_amount", nullable = false, precision = 12, scale = 3)),
        @AttributeOverride(name = "currency", column = @Column(name = "line_total_currency", nullable = false))
    })
    private Money lineTotal;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status")
    private OrderItemStatus itemStatus;

    @Builder.Default
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItemModifier> modifiers = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItemRemovedIngredient> removedIngredients = new HashSet<>();

    public void addModifier(OrderItemModifier modifier) {
        modifiers.add(modifier);
        modifier.setOrderItem(this);
    }

    public void addRemovedIngredient(OrderItemRemovedIngredient removedIngredient) {
        removedIngredients.add(removedIngredient);
        removedIngredient.setOrderItem(this);
    }
}
