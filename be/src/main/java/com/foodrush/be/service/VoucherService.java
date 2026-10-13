package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.response.IssuedVoucherResponse;
import com.foodrush.be.dto.response.RedemptionStatusResponse;
import com.foodrush.be.dto.response.VoucherResponse;
import com.foodrush.be.entity.IssuedVoucher;
import com.foodrush.be.entity.Voucher;
import com.foodrush.be.entity.enums.IssuedVoucherStatus;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.IssuedVoucherRepository;
import com.foodrush.be.repository.UserRepository;
import com.foodrush.be.repository.VoucherRepository;
import com.foodrush.be.util.CouponCodeGenerator;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final IssuedVoucherRepository issuedVoucherRepository;
    private final UserRepository userRepository;
    private final CoinWalletService coinWalletService;
    private final CouponCodeGenerator codeGenerator;
    private final ZoneId appZoneId;

    @Transactional(readOnly = true)
    public List<VoucherResponse> listActive() {
        return voucherRepository.findAllByOrderByIdAsc().stream()
                .filter(Voucher::isActive)
                .map(v -> VoucherResponse.from(v, issuedVoucherRepository.countByVoucherId(v.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> listAll() {
        return voucherRepository.findAllByOrderByIdAsc().stream()
                .map(v -> VoucherResponse.from(v, issuedVoucherRepository.countByVoucherId(v.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RedemptionStatusResponse redemptionStatus(Long userId) {
        LocalDate today = LocalDate.now(appZoneId);
        boolean redeemedToday = issuedVoucherRepository.existsByUserIdAndRedemptionDate(userId, today);
        LocalDate lastDate = issuedVoucherRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .map(IssuedVoucher::getRedemptionDate)
                .max(LocalDate::compareTo)
                .orElse(null);
        if (redeemedToday) {
            return new RedemptionStatusResponse(false, lastDate, "You have already redeemed a voucher today. Try again tomorrow.");
        }
        return new RedemptionStatusResponse(true, lastDate, "You can redeem a voucher today.");
    }

    @Transactional
    public IssuedVoucherResponse redeem(Long userId, Long voucherId) {
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Voucher voucher = voucherRepository.findByIdForUpdate(voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (!voucher.isActive()) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }
        if (voucher.getRemainingStock() <= 0) {
            throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
        }

        LocalDate today = LocalDate.now(appZoneId);
        if (issuedVoucherRepository.existsByUserIdAndRedemptionDate(userId, today)) {
            throw new AppException(ErrorCode.VOUCHER_REDEEMED_TODAY);
        }

        IssuedVoucher issued = IssuedVoucher.builder()
                .code(nextUniqueCode())
                .user(userRepository.getReferenceById(userId))
                .voucher(voucher)
                .status(IssuedVoucherStatus.ISSUED)
                .redemptionDate(today)
                .issuedAt(java.time.Instant.now())
                .build();

        try {
            issuedVoucherRepository.saveAndFlush(issued);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.VOUCHER_REDEEMED_TODAY);
        }

        voucher.setRemainingStock(voucher.getRemainingStock() - 1);
        voucherRepository.save(voucher);

        coinWalletService.spendForVoucher(userId, issued, voucher.getPriceCoins(),
                "Redeemed voucher " + voucher.getName());

        return IssuedVoucherResponse.from(issued);
    }

    @Transactional(readOnly = true)
    public List<IssuedVoucherResponse> myVouchers(Long userId) {
        return issuedVoucherRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .map(IssuedVoucherResponse::from)
                .toList();
    }

    private String nextUniqueCode() {
        // Best-effort uniqueness; DB unique constraint protects against collisions.
        return codeGenerator.generate();
    }
}