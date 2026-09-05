package com.example.coresto.domain.catalog;

import com.example.coresto.domain.common.BaseEntity;
import com.example.coresto.domain.common.SelectionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "modifier_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "options")
public class ModifierGroup extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type")
    private SelectionType selectionType;

    @Column(name = "min_select")
    private int minSelect;

    @Column(name = "max_select")
    private int maxSelect;

    @Column(name = "required")
    private boolean required;

    @Column(name = "display_order")
    private int displayOrder;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ModifierOption> options = new HashSet<>();

    public void addOption(ModifierOption o) {
        this.options.add(o);
        o.setGroup(this);
    }

    public void removeOption(ModifierOption o) {
        this.options.remove(o);
        o.setGroup(null);
    }
}
