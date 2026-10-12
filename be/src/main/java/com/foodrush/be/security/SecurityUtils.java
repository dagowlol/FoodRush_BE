package com.foodrush.be.security;

import com.foodrush.be.common.ErrorCode;
import com.foodrush.be.exception.AppException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }
}
