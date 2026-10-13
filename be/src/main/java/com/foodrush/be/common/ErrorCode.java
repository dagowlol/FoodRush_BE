package com.foodrush.be.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ"),
    INSUFFICIENT_SCORE(HttpStatus.BAD_REQUEST, "Bạn chưa đủ điểm để nhận phần thưởng này"),
    INVALID_DISCOUNT_OPTION(HttpStatus.BAD_REQUEST, "Phương án giảm giá này không khả dụng với số điểm của bạn"),

    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để tiếp tục"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Tài khoản này đã bị khoá. Vui lòng liên hệ hỗ trợ."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy phiên chơi"),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy mã giảm giá"),
    VOUCHER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy voucher"),

    USERNAME_TAKEN(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại"),
    EMAIL_TAKEN(HttpStatus.CONFLICT, "Email này đã được đăng ký"),
    SESSION_FINISHED(HttpStatus.CONFLICT, "Phiên chơi đã kết thúc"),
    COUPON_ALREADY_CLAIMED_TODAY(HttpStatus.CONFLICT, "Bạn đã nhận mã giảm giá hôm nay"),
    COUPON_ALREADY_REDEEMED(HttpStatus.CONFLICT, "Bạn đã đổi mã giảm giá này rồi"),
    INSUFFICIENT_COINS(HttpStatus.BAD_REQUEST, "Bạn không đủ coin để đổi voucher này"),
    VOUCHER_INACTIVE(HttpStatus.BAD_REQUEST, "Voucher này hiện không hoạt động"),
    VOUCHER_OUT_OF_STOCK(HttpStatus.CONFLICT, "Voucher này đã hết"),
    VOUCHER_REDEEMED_TODAY(HttpStatus.CONFLICT, "Bạn đã đổi voucher này hôm nay"),
    INVALID_INVENTORY(HttpStatus.BAD_REQUEST, "Số lượng không hợp lệ"),
    COUPON_EXPIRED(HttpStatus.CONFLICT, "Mã giảm giá đã hết hạn"),
    DATA_CONFLICT(HttpStatus.CONFLICT, "Dữ liệu đang xung đột với trạng thái hiện tại"),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi không mong muốn");

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