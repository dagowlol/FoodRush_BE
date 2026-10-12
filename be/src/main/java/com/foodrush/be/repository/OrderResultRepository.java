package com.foodrush.be.repository;

import com.foodrush.be.entity.OrderResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderResultRepository extends JpaRepository<OrderResult, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<OrderResult> findByOrderId(Long orderId);

    @Query("select r from OrderResult r where r.order.session.id = :sessionId order by r.servedAt asc")
    List<OrderResult> findBySessionIdOrderByServedAtAsc(@Param("sessionId") Long sessionId);
}
