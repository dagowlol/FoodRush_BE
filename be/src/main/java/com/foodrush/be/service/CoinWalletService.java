package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.response.CoinTransactionResponse;
import com.foodrush.be.dto.response.WalletResponse;
import com.foodrush.be.entity.CoinTransaction;
import com.foodrush.be.entity.GameSession;
import com.foodrush.be.entity.IssuedVoucher;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.SessionStatus;
import com.foodrush.be.entity.enums.CoinTransactionType;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.CoinTransactionRepository;
import com.foodrush.be.repository.GameSessionRepository;
import com.foodrush.be.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoinWalletService {

    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final GameSessionRepository gameSessionRepository;

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return new WalletResponse(user.getCoinBalance());
    }

    @Transactional(readOnly = true)
    public List<CoinTransactionResponse> getTransactions(Long userId) {
        return coinTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(CoinTransactionResponse::from)
                .toList();
    }

    @Transactional
    public int awardForSession(Long userId, Long sessionId, int finalScore) {
        if (finalScore <= 0) {
            return 0;
        }
        if (coinTransactionRepository.existsByGameSession_Id(sessionId)) {
            return 0;
        }

        GameSession session = gameSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() != SessionStatus.FINISHED) {
            throw new AppException(ErrorCode.SESSION_NOT_FOUND);
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        int coinsEarned = finalScore / 10;
        if (coinsEarned < 0) {
            coinsEarned = 0;
        }

        long newBalance = user.getCoinBalance() + coinsEarned;
        user.setCoinBalance(newBalance);
        userRepository.save(user);

        CoinTransaction transaction = CoinTransaction.builder()
                .user(user)
                .amount(coinsEarned)
                .balanceAfter(newBalance)
                .type(CoinTransactionType.GAME_REWARD)
                .description("Game session reward")
                .gameSession(session)
                .build();
        try {
            coinTransactionRepository.saveAndFlush(transaction);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return 0;
        }
        return coinsEarned;
    }

    @Transactional
    public long spendCoins(Long userId, Long issuedVoucherId, int priceCoins, String description) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getCoinBalance() < priceCoins) {
            throw new AppException(ErrorCode.INSUFFICIENT_COINS);
        }

        long newBalance = user.getCoinBalance() - priceCoins;
        if (newBalance < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_COINS);
        }
        user.setCoinBalance(newBalance);
        userRepository.save(user);

        IssuedVoucher issuedVoucher = null;
        if (issuedVoucherId != null) {
            // populated by caller via service? We'll keep null here to avoid circular; service creates tx separately.
        }

        CoinTransaction transaction = CoinTransaction.builder()
                .user(user)
                .amount(-priceCoins)
                .balanceAfter(newBalance)
                .type(CoinTransactionType.VOUCHER_REDEMPTION)
                .description(description)
                .build();
        coinTransactionRepository.saveAndFlush(transaction);
        return newBalance;
    }

    @Transactional
    public long spendForVoucher(Long userId, IssuedVoucher issuedVoucher, int priceCoins, String description) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getCoinBalance() < priceCoins) {
            throw new AppException(ErrorCode.INSUFFICIENT_COINS);
        }

        long newBalance = user.getCoinBalance() - priceCoins;
        if (newBalance < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_COINS);
        }
        user.setCoinBalance(newBalance);
        userRepository.save(user);

        CoinTransaction transaction = CoinTransaction.builder()
                .user(user)
                .amount(-priceCoins)
                .balanceAfter(newBalance)
                .type(CoinTransactionType.VOUCHER_REDEMPTION)
                .description(description)
                .issuedVoucher(issuedVoucher)
                .build();
        coinTransactionRepository.saveAndFlush(transaction);
        return newBalance;
    }
}