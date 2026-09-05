package com.example.coresto.domain.identity;

import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.tenancy.Branch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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
 * branch.organisation must equal member.organisation.
 * branch.id is a Keycloak organization group UUID.
 */
@Entity
@Table(
        name = "member_branch_access",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_branch_access_member_branch", columnNames = {"member_id", "branch_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class MemberBranchAccess extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private OrganisationMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}
