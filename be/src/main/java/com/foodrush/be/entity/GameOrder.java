package com.foodrush.be.entity;

import com.foodrush.be.entity.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        name = "game_orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_game_orders_session_sequence",
                        columnNames = {"session_id", "sequence_no"})
        },
        indexes = {
                @Index(name = "idx_game_orders_session_status", columnList = "session_id, status")
        })
public class GameOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_game_orders_session"))
    private GameSession session;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "game_order_items",
            joinColumns = @JoinColumn(name = "order_id",
                    foreignKey = @ForeignKey(name = "fk_game_order_items_order")),
            inverseJoinColumns = @JoinColumn(name = "food_item_id",
                    foreignKey = @ForeignKey(name = "fk_game_order_items_food")),
            uniqueConstraints = @UniqueConstraint(name = "uk_game_order_items",
                    columnNames = {"order_id", "food_item_id"}))
    private Set<FoodItem> requiredItems = new LinkedHashSet<>();
}
