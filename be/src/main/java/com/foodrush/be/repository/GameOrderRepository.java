package com.foodrush.be.repository;

import com.foodrush.be.entity.GameOrder;
import com.foodrush.be.entity.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameOrderRepository extends JpaRepository<GameOrder, Long> {

    Optional<GameOrder> findFirstBySessionIdAndStatusOrderBySequenceNoDesc(Long sessionId, OrderStatus status);

    Optional<GameOrder> findByIdAndSessionId(Long id, Long sessionId);

    List<GameOrder> findBySessionIdOrderBySequenceNoAsc(Long sessionId);

    @Query("select coalesce(max(o.sequenceNo), 0) from GameOrder o where o.session.id = :sessionId")
    int findMaxSequenceNo(@Param("sessionId") Long sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from GameOrder o where o.id = :id")
    Optional<GameOrder> findByIdForUpdate(@Param("id") Long id);
}
