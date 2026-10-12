package com.foodrush.be.repository;

import com.foodrush.be.entity.FoodItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findAllByActiveTrueOrderByIdAsc();

    Optional<FoodItem> findByCode(String code);

    boolean existsByCode(String code);
}
