package com.example.coresto.domain.tenancy;

import com.example.coresto.domain.catalog.MenuBranch;
import com.example.coresto.domain.catalog.ProductBranchAvailability;
import com.example.coresto.domain.common.Address;
import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.BranchStatus;
import com.example.coresto.domain.common.GeoPoint;
import com.example.coresto.domain.common.ServiceMode;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.UniqueConstraint;
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

/**
 * Sync contract - After POST /api/organizations/{orgId}/groups, persist with same Keycloak group UUID as id.
 */
@Entity
@Table(
        name = "branches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organisation_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"organisation", "enabledServiceModes", "openingHours", "tables", "qrCodes", "menuBranches", "productAvailabilities"})
public class Branch extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "name", nullable = false)
    private String name;

    @Embedded
    private Address address;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude"))
    })
    private GeoPoint location;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "timezone")
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BranchStatus status;

    @ElementCollection
    @CollectionTable(
            name = "branch_service_modes",
            joinColumns = @JoinColumn(name = "branch_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode")
    @Builder.Default
    private Set<ServiceMode> enabledServiceModes = new HashSet<>();

    @OneToMany(mappedBy = "branch")
    @Builder.Default
    private Set<OpeningHours> openingHours = new HashSet<>();

    @OneToMany(mappedBy = "branch")
    @Builder.Default
    private Set<DiningTable> tables = new HashSet<>();

    @OneToMany(mappedBy = "branch")
    @Builder.Default
    private Set<QrCode> qrCodes = new HashSet<>();

    @OneToMany(mappedBy = "branch")
    @Builder.Default
    private Set<MenuBranch> menuBranches = new HashSet<>();

    @OneToMany(mappedBy = "branch")
    @Builder.Default
    private Set<ProductBranchAvailability> productAvailabilities = new HashSet<>();

    public void addOpeningHours(OpeningHours oh) {
        openingHours.add(oh);
        oh.setBranch(this);
    }

    public void removeOpeningHours(OpeningHours oh) {
        openingHours.remove(oh);
        oh.setBranch(null);
    }

    public void addTable(DiningTable table) {
        tables.add(table);
        table.setBranch(this);
        if (this.organisation != null) {
            table.setOrganisation(this.organisation);
        }
    }

    public void removeTable(DiningTable table) {
        tables.remove(table);
        table.setBranch(null);
    }

    public void addQrCode(QrCode qrCode) {
        qrCodes.add(qrCode);
        qrCode.setBranch(this);
        if (this.organisation != null) {
            qrCode.setOrganisation(this.organisation);
        }
    }

    public void removeQrCode(QrCode qrCode) {
        qrCodes.remove(qrCode);
        qrCode.setBranch(null);
    }
}
