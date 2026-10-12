package com.foodrush.be.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    INSUFFICIENT_SCORE(HttpStatus.BAD_REQUEST, "You do not have enough points for a reward yet"),
    INVALID_DISCOUNT_OPTION(HttpStatus.BAD_REQUEST, "Requested discount is not available for your score"),

    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password"),

    FORBIDDEN(HttpStatus.FORBIDDEN, "You are not allowed to perform this action"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "Game session not found"),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "Coupon not found"),

    USERNAME_TAKEN(HttpStatus.CONFLICT, "Username is already taken"),
    EMAIL_TAKEN(HttpStatus.CONFLICT, "Email is already registered"),
    SESSION_FINISHED(HttpStatus.CONFLICT, "The game session has already been finished"),
    COUPON_ALREADY_CLAIMED_TODAY(HttpStatus.CONFLICT, "You have already claimed a coupon today"),
    COUPON_ALREADY_REDEEMED(HttpStatus.CONFLICT, "This coupon has already been redeemed"),
    COUPON_EXPIRED(HttpStatus.CONFLICT, "This coupon has expired"),
    DATA_CONFLICT(HttpStatus.CONFLICT, "The request conflicts with the current state of the data"),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
