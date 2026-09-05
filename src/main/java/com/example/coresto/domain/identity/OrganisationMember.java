package com.example.coresto.domain.identity;

import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.MemberStatus;
import com.example.coresto.domain.common.Role;
import com.example.coresto.domain.tenancy.Organisation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Authorization model: Realm ROLE_ADMIN = platform operator only.
 * Realm ROLE_USER is ignored.
 * App roles live on OrganisationMember.role: OWNER, SUPER_MANAGER, BRANCH_MANAGER, WORKER.
 * OWNER/SUPER_MANAGER: MemberBranchAccess may be empty (= all branches).
 * BRANCH_MANAGER/WORKER: at least one MemberBranchAccess required.
 * Staff APIs authorize with OrganisationMember + MemberBranchAccess.
 */
@Entity
@Table(
        name = "organisation_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_organisation_members_org_user", columnNames = {"organisation_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "branchAccesses")
public class OrganisationMember extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MemberBranchAccess> branchAccesses = new HashSet<>();

    public void addBranchAccess(MemberBranchAccess branchAccess) {
        branchAccesses.add(branchAccess);
        branchAccess.setMember(this);
    }

    public void removeBranchAccess(MemberBranchAccess branchAccess) {
        branchAccesses.remove(branchAccess);
        branchAccess.setMember(null);
    }
}
