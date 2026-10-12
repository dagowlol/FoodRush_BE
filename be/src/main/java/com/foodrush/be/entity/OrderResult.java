package com.foodrush.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "order_results",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_results_order", columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_order_results_user", columnList = "user_id")
        })
public class OrderResult extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_results_order"))
    private GameOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_results_user"))
    private User user;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "score_awarded", nullable = false)
    private int scoreAwarded;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "order_result_items",
            joinColumns = @JoinColumn(name = "result_id",
                    foreignKey = @ForeignKey(name = "fk_order_result_items_result")),
            inverseJoinColumns = @JoinColumn(name = "food_item_id",
                    foreignKey = @ForeignKey(name = "fk_order_result_items_food")),
            uniqueConstraints = @UniqueConstraint(name = "uk_order_result_items",
                    columnNames = {"result_id", "food_item_id"}))
    private Set<FoodItem> servedItems = new LinkedHashSet<>();

    @Column(name = "served_at", nullable = false)
    private Instant servedAt;
}
