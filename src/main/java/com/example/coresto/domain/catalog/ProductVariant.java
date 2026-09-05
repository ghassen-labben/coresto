package com.example.coresto.domain.catalog;

import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.Money;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "product_variants", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class ProductVariant extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(name = "sku")
    private String sku;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_override_amount", precision = 12, scale = 3)),
            @AttributeOverride(name = "currency", column = @Column(name = "price_override_currency"))
    })
    private Money priceOverride;

    @Column(name = "default_variant")
    private boolean defaultVariant;
}
