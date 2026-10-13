package com.foodrush.be.service;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.common.PageResponse;
import com.foodrush.be.dto.request.CreateUserRequest;
import com.foodrush.be.dto.request.UpdateUserRequest;
import com.foodrush.be.dto.response.AdminUserResponse;
import com.foodrush.be.dto.response.AdminUserStatsResponse;
import com.foodrush.be.entity.User;
import com.foodrush.be.entity.enums.Role;
import com.foodrush.be.exception.AppException;
import com.foodrush.be.repository.UserRepository;
import com.foodrush.be.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> list(String keyword, Role role, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (!kw.isEmpty()) {
                String pattern = "%" + kw + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("displayName")), pattern)));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AdminUserResponse> result = userRepository
                .findAll(spec, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id")))
                .map(AdminUserResponse::from);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public AdminUserStatsResponse stats() {
        return new AdminUserStatsResponse(
                userRepository.count(),
                userRepository.countByActiveFalse(),
                userRepository.countByRole(Role.STAFF),
                userRepository.sumCoinBalance());
    }

    @Transactional(readOnly = true)
    public AdminUserResponse get(Long id) {
        return AdminUserResponse.from(findById(id));
    }

    @Transactional
    public AdminUserResponse create(CreateUserRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.USERNAME_TAKEN);
        }
        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_TAKEN);
        }

        String displayName = StringUtils.hasText(request.displayName())
                ? request.displayName().trim()
                : username;
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(displayName)
                .role(request.role())
                .coinBalance(request.coinBalance() == null ? 0L : request.coinBalance())
                .totalScore(0L)
                .build();
        return AdminUserResponse.from(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse update(Long id, UpdateUserRequest request) {
        User user = findById(id);
        boolean self = user.getId().equals(SecurityUtils.currentUserId());

        if (request.role() != null && request.role() != user.getRole()) {
            // Guard against an admin locking themselves (or the last admin) out.
            if (self) {
                throw new AppException(ErrorCode.FORBIDDEN, "Bạn không thể tự đổi vai trò của chính mình");
            }
            if (user.getRole() == Role.ADMIN && countActiveAdmins() <= 1) {
                throw new AppException(ErrorCode.DATA_CONFLICT, "Phải còn ít nhất một quản trị viên");
            }
            user.setRole(request.role());
        }

        if (request.coinBalance() != null) {
            user.setCoinBalance(request.coinBalance());
        }

        if (request.active() != null && request.active() != user.isActive()) {
            // Locking yourself out would be a one-way trip, so block it explicitly.
            if (self && !request.active()) {
                throw new AppException(ErrorCode.FORBIDDEN, "Bạn không thể tự khoá tài khoản của chính mình");
            }
            // Never let the last usable admin be locked, or nobody can unlock it again.
            if (user.getRole() == Role.ADMIN && user.isActive() && !request.active()
                    && countActiveAdmins() <= 1) {
                throw new AppException(ErrorCode.DATA_CONFLICT, "Phải còn ít nhất một quản trị viên đang hoạt động");
            }
            user.setActive(request.active());
        }

        userRepository.save(user);
        return AdminUserResponse.from(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private long countActiveAdmins() {
        return userRepository.findAll((root, query, cb) -> cb.and(
                        cb.equal(root.get("role"), Role.ADMIN),
                        cb.isTrue(root.get("active"))))
                .stream()
                .count();
    }
}