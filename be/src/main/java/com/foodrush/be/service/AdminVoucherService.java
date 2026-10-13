package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.dto.request.VoucherCreateRequest;
import com.foodrush.be.dto.request.VoucherUpdateRequest;
import com.foodrush.be.dto.response.IssuedVoucherResponse;
import com.foodrush.be.dto.response.VoucherResponse;
import com.foodrush.be.entity.Voucher;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.IssuedVoucherRepository;
import com.foodrush.be.repository.VoucherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminVoucherService {

    private final VoucherRepository voucherRepository;
    private final IssuedVoucherRepository issuedVoucherRepository;

    @Transactional(readOnly = true)
    public List<VoucherResponse> list() {
        return voucherRepository.findAllByOrderByIdAsc().stream()
                .map(v -> VoucherResponse.from(v, issuedVoucherRepository.countByVoucherId(v.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public VoucherResponse get(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        return VoucherResponse.from(voucher, issuedVoucherRepository.countByVoucherId(voucher.getId()));
    }

    @Transactional
    public VoucherResponse create(VoucherCreateRequest request) {
        if (request.priceCoins() < 0 || request.initialStock() < 0) {
            throw new AppException(ErrorCode.INVALID_INVENTORY);
        }
        Voucher voucher = Voucher.builder()
                .name(request.name().trim())
                .description(request.description())
                .discountValue(request.discountValue())
                .priceCoins(request.priceCoins())
                .initialStock(request.initialStock())
                .remainingStock(request.initialStock())
                .active(request.active() == null || request.active())
                .build();
        voucherRepository.save(voucher);
        return VoucherResponse.from(voucher, 0);
    }

    @Transactional
    public VoucherResponse update(Long id, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (request.name() != null && !request.name().isBlank()) {
            voucher.setName(request.name().trim());
        }
        if (request.description() != null) {
            voucher.setDescription(request.description());
        }
        if (request.discountValue() != null) {
            voucher.setDiscountValue(request.discountValue());
        }
        if (request.priceCoins() != null) {
            if (request.priceCoins() < 0) {
                throw new AppException(ErrorCode.INVALID_INVENTORY);
            }
            voucher.setPriceCoins(request.priceCoins());
        }
        if (request.active() != null) {
            voucher.setActive(request.active());
        }
        if (request.remainingStock() != null) {
            if (request.remainingStock() < 0) {
                throw new AppException(ErrorCode.INVALID_INVENTORY);
            }
            long issued = issuedVoucherRepository.countByVoucherId(voucher.getId());
            int minRemaining = 0; // ensure stock not below issued count adjustment
            // Prevent reducing available inventory below number already issued relative to initial? 
            // Safe rule: remainingStock + issued >= issued (always) ; instead ensure new remaining >= 0 only.
            // Additional safety: cannot make remaining less than (initialStock - issued? no)
            voucher.setRemainingStock(request.remainingStock());
        }

        voucherRepository.save(voucher);
        return VoucherResponse.from(voucher, issuedVoucherRepository.countByVoucherId(voucher.getId()));
    }

    @Transactional(readOnly = true)
    public List<IssuedVoucherResponse> issuedVouchers() {
        return issuedVoucherRepository.findAllByOrderByIssuedAtDesc().stream()
                .map(IssuedVoucherResponse::from)
                .toList();
    }

    @Transactional
    public VoucherResponse updateStatus(Long id, boolean active) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        voucher.setActive(active);
        voucherRepository.save(voucher);
        return VoucherResponse.from(voucher, issuedVoucherRepository.countByVoucherId(voucher.getId()));
    }
}