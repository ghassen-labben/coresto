package com.example.coresto.domain.catalog;

import com.example.coresto.domain.common.Allergen;
import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.DietaryTag;
import com.example.coresto.domain.common.MediaAsset;
import com.example.coresto.domain.common.Money;
import com.example.coresto.domain.common.ProductStatus;
import com.example.coresto.domain.tenancy.Organisation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"variants", "ingredients", "modifierGroups", "availabilities", "productCategories", "allergens", "dietaryTags"})
public class Product extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sku")
    private String sku;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "base_price_amount", nullable = false, precision = 12, scale = 3)),
            @AttributeOverride(name = "currency", column = @Column(name = "base_price_currency", nullable = false))
    })
    private Money basePrice;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private MediaAsset image;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatus status;

    @Column(name = "prep_minutes")
    private Integer prepMinutes;

    @Column(name = "featured")
    private boolean featured;

    @ElementCollection
    @CollectionTable(name = "product_allergens", joinColumns = @JoinColumn(name = "product_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen")
    @Builder.Default
    private Set<Allergen> allergens = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "product_dietary_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_tag")
    @Builder.Default
    private Set<DietaryTag> dietaryTags = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductVariant> variants = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductIngredient> ingredients = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ModifierGroup> modifierGroups = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductBranchAvailability> availabilities = new HashSet<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private Set<ProductCategory> productCategories = new HashSet<>();

    public void addVariant(ProductVariant variant) {
        this.variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        this.variants.remove(variant);
        variant.setProduct(null);
    }

    public void addIngredient(ProductIngredient ingredient) {
        this.ingredients.add(ingredient);
        ingredient.setProduct(this);
    }

    public void removeIngredient(ProductIngredient ingredient) {
        this.ingredients.remove(ingredient);
        ingredient.setProduct(null);
    }

    public void addModifierGroup(ModifierGroup group) {
        this.modifierGroups.add(group);
        group.setProduct(this);
    }

    public void removeModifierGroup(ModifierGroup group) {
        this.modifierGroups.remove(group);
        group.setProduct(null);
    }

    public void addAvailability(ProductBranchAvailability availability) {
        this.availabilities.add(availability);
        availability.setProduct(this);
    }

    public void removeAvailability(ProductBranchAvailability availability) {
        this.availabilities.remove(availability);
        availability.setProduct(null);
    }
}
