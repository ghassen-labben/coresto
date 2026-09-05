package com.example.coresto.domain.tenancy;

import com.example.coresto.domain.catalog.Menu;
import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.QrTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

import java.util.UUID;

/**
 * QR Code entity representing table or menu direct-access tokens.
 * Validation contract / Business rule:
 * - If targetType == TABLE => table (table_id) must not be null.
 * - If targetType == MENU => menu (menu_id) must not be null.
 */
@Entity
@Table(
        name = "qr_codes",
        uniqueConstraints = @UniqueConstraint(columnNames = "public_token"),
        indexes = @Index(columnList = "public_token")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"organisation", "branch", "table", "menu"})
public class QrCode extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
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
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private QrTargetType targetType;

    @Column(name = "public_token", nullable = false)
    private String publicToken;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
