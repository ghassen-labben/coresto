package com.example.coresto.domain.tenancy;

import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.ServiceMode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "organisation_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"organisation", "enabledServiceModes"})
public class OrganisationSettings extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @Column(name = "tax_inclusive_pricing", nullable = false)
    @Builder.Default
    private boolean taxInclusivePricing = false;

    @Column(name = "auto_accept_orders", nullable = false)
    @Builder.Default
    private boolean autoAcceptOrders = false;

    @Column(name = "pay_before_prepare", nullable = false)
    @Builder.Default
    private boolean payBeforePrepare = false;

    @Column(name = "tipping_enabled", nullable = false)
    @Builder.Default
    private boolean tippingEnabled = false;

    @Column(name = "guest_notes_enabled", nullable = false)
    @Builder.Default
    private boolean guestNotesEnabled = false;

    @ElementCollection
    @CollectionTable(
            name = "organisation_service_modes",
            joinColumns = @JoinColumn(name = "organisation_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode")
    @Builder.Default
    private Set<ServiceMode> enabledServiceModes = new HashSet<>();
}
