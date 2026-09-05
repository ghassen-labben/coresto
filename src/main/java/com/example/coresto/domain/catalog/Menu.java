package com.example.coresto.domain.catalog;

import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.MenuStatus;
import com.example.coresto.domain.common.TimeWindow;
import com.example.coresto.domain.tenancy.Organisation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"categories", "menuBranches", "daysOfWeek"})
public class Menu extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MenuStatus status;

    @Column(name = "display_order")
    private int displayOrder;

    @Embedded
    private TimeWindow availability;

    @ElementCollection
    @CollectionTable(name = "menu_days", joinColumns = @JoinColumn(name = "menu_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    @Builder.Default
    private Set<DayOfWeek> daysOfWeek = new HashSet<>();

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MenuBranch> menuBranches = new HashSet<>();

    public void addCategory(Category category) {
        this.categories.add(category);
        category.setMenu(this);
    }

    public void removeCategory(Category category) {
        this.categories.remove(category);
        category.setMenu(null);
    }

    public void addMenuBranch(MenuBranch menuBranch) {
        this.menuBranches.add(menuBranch);
        menuBranch.setMenu(this);
    }

    public void removeMenuBranch(MenuBranch menuBranch) {
        this.menuBranches.remove(menuBranch);
        menuBranch.setMenu(null);
    }
}
