package com.example.coresto.domain.ordering;

import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.Money;
import com.example.coresto.domain.common.OrderStatus;
import com.example.coresto.domain.common.ServiceMode;
import com.example.coresto.domain.identity.User;
import com.example.coresto.domain.tenancy.Branch;
import com.example.coresto.domain.tenancy.DiningTable;
import com.example.coresto.domain.tenancy.Organisation;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "orders",
    uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "public_number"}),
    indexes = {
        @Index(columnList = "branch_id, status, placed_at DESC"),
        @Index(columnList = "guest_device_id, placed_at DESC")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"items", "payments"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Order extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private DiningTable table;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private DiningSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_device_id", nullable = false)
    private GuestDevice guestDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_worker_id")
    private User assignedWorker;

    @Column(name = "public_number", nullable = false)
    private String publicNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode")
    private ServiceMode serviceMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount", precision = 12, scale = 3)),
        @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency"))
    })
    private Money subtotal;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "tax_amount", precision = 12, scale = 3)),
        @AttributeOverride(name = "currency", column = @Column(name = "tax_currency"))
    })
    private Money taxAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "discount_amount", precision = 12, scale = 3)),
        @AttributeOverride(name = "currency", column = @Column(name = "discount_currency"))
    })
    private Money discountAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "tip_amount", precision = 12, scale = 3)),
        @AttributeOverride(name = "currency", column = @Column(name = "tip_currency"))
    })
    private Money tipAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount", precision = 12, scale = 3)),
        @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
    })
    private Money total;

    @Column(name = "placed_at")
    private Instant placedAt;

    @Column(name = "estimated_ready_at")
    private Instant estimatedReadyAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new HashSet<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setOrder(this);
    }
}
