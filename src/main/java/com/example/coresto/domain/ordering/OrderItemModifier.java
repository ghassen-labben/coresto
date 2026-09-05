package com.example.coresto.domain.ordering;

import com.example.coresto.domain.catalog.ModifierOption;
import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "order_item_modifiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class OrderItemModifier extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_option_id")
    private ModifierOption modifierOption;

    @Column(name = "name_snapshot", nullable = false)
    private String nameSnapshot;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "extra_price_snapshot_amount", precision = 12, scale = 3)),
        @AttributeOverride(name = "currency", column = @Column(name = "extra_price_snapshot_currency"))
    })
    private Money extraPriceSnapshot;
}
