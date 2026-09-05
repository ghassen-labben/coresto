package com.example.coresto.domain.catalog;

import com.example.coresto.domain.common.AvailabilityStatus;
import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.tenancy.Branch;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Missing row = IN_STOCK.
 */
@Entity
@Table(name = "product_branch_availability", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "branch_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class ProductBranchAvailability extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AvailabilityStatus status;
}
