package com.example.coresto.domain.catalog;

import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.tenancy.Branch;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * menu.organisation must equal branch.organisation.
 */
@Entity
@Table(name = "menu_branches", uniqueConstraints = @UniqueConstraint(columnNames = {"menu_id", "branch_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class MenuBranch extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "active")
    private boolean active;
}
