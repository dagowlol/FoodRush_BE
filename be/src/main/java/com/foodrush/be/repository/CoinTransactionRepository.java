package com.foodrush.be.repository;

import com.foodrush.be.entity.CoinTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {

    List<CoinTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByGameSession_Id(Long gameSessionId);
}