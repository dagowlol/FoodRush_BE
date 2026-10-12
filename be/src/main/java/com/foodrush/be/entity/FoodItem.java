package com.foodrush.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        name = "food_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_food_items_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_food_items_name", columnNames = "name")
        })
public class FoodItem extends BaseEntity {

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 16)
    private String emoji;

    @Builder.Default
    @Column(name = "base_points", nullable = false)
    private int basePoints = 1;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
