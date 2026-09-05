package com.example.coresto.domain.ordering;

import com.example.coresto.domain.catalog.Product;
import com.example.coresto.domain.catalog.ProductVariant;
import com.example.coresto.domain.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"modifiers", "removedIngredients"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class CartItem extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItemModifier> modifiers = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartRemovedIngredient> removedIngredients = new HashSet<>();

    public void addModifier(CartItemModifier modifier) {
        modifiers.add(modifier);
        modifier.setCartItem(this);
    }

    public void removeModifier(CartItemModifier modifier) {
        modifiers.remove(modifier);
        modifier.setCartItem(null);
    }

    public void addRemovedIngredient(CartRemovedIngredient removedIngredient) {
        removedIngredients.add(removedIngredient);
        removedIngredient.setCartItem(this);
    }

    public void removeRemovedIngredient(CartRemovedIngredient removedIngredient) {
        removedIngredients.remove(removedIngredient);
        removedIngredient.setCartItem(null);
    }
}
