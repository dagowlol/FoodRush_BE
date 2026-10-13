package com.foodrush.be.repository;

import com.foodrush.be.entity.CoinTransaction;
import com.foodrush.be.entity.enums.CoinTransactionType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {

    List<CoinTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByGameSession_Id(Long gameSessionId);

    @Query("select coalesce(sum(ct.amount), 0) from CoinTransaction ct " +
           "where ct.user.id = :userId and ct.type = :type " +
           "and ct.createdAt >= :start and ct.createdAt < :end")
    long sumAmountByUserAndTypeAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("type") CoinTransactionType type,
            @Param("start") Instant start,
            @Param("end") Instant end);
}