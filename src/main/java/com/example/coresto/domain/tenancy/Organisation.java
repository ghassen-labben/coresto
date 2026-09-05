package com.example.coresto.domain.tenancy;

import com.example.coresto.domain.catalog.Menu;
import com.example.coresto.domain.catalog.Product;
import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.CurrencyCode;
import com.example.coresto.domain.common.LocaleCode;
import com.example.coresto.domain.common.MediaAsset;
import com.example.coresto.domain.common.OrganisationStatus;
import com.example.coresto.domain.common.OrganisationType;
import com.example.coresto.domain.identity.Invitation;
import com.example.coresto.domain.identity.OrganisationMember;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
 * Sync contract - After POST /api/organizations, persist with same Keycloak org UUID as id.
 */
@Entity
@Table(
        name = "organisations",
        uniqueConstraints = @UniqueConstraint(columnNames = "alias")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"settings", "branches", "members", "menus", "products", "invitations", "domains", "logo"})
public class Organisation extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "alias", nullable = false, unique = true)
    private String alias;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @ElementCollection
    @CollectionTable(
            name = "organisation_domains",
            joinColumns = @JoinColumn(name = "organisation_id")
    )
    @Column(name = "domain")
    @Builder.Default
    private Set<String> domains = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private OrganisationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrganisationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_id")
    private MediaAsset logo;

    @Column(name = "country")
    private String country;

    @Column(name = "timezone")
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_currency")
    private CurrencyCode defaultCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_locale")
    private LocaleCode defaultLocale;

    @Column(name = "tax_id")
    private String taxId;

    @OneToOne(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrganisationSettings settings;

    @OneToMany(mappedBy = "organisation")
    @Builder.Default
    private Set<Branch> branches = new HashSet<>();

    @OneToMany(mappedBy = "organisation")
    @Builder.Default
    private Set<OrganisationMember> members = new HashSet<>();

    @OneToMany(mappedBy = "organisation")
    @Builder.Default
    private Set<Menu> menus = new HashSet<>();

    @OneToMany(mappedBy = "organisation")
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "organisation")
    @Builder.Default
    private Set<Invitation> invitations = new HashSet<>();

    public void addBranch(Branch branch) {
        branches.add(branch);
        branch.setOrganisation(this);
    }

    public void removeBranch(Branch branch) {
        branches.remove(branch);
        branch.setOrganisation(null);
    }

    public void addMember(OrganisationMember member) {
        members.add(member);
        member.setOrganisation(this);
    }

    public void removeMember(OrganisationMember member) {
        members.remove(member);
        member.setOrganisation(null);
    }

    public void setSettings(OrganisationSettings settings) {
        this.settings = settings;
        if (settings != null) {
            settings.setOrganisation(this);
        }
    }
}
