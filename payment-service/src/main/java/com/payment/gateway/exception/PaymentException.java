package com.payment.gateway.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {

    private final String errorCode;
    private final Object details;

    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public PaymentException(String message, String errorCode, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public PaymentException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public static final String TRANSACTION_NOT_FOUND = "PAYMENT_001";
    public static final String PAYMENT_METHOD_NOT_FOUND = "PAYMENT_002";
    public static final String INVALID_PAYMENT_STATE = "PAYMENT_003";
    public static final String PAYMENT_PROCESSING_FAILED = "PAYMENT_004";
    public static final String INSUFFICIENT_FUNDS = "PAYMENT_005";
    public static final String REFUND_NOT_ALLOWED = "PAYMENT_006";
    public static final String REFUND_AMOUNT_EXCEEDED = "PAYMENT_007";
    public static final String PAYMENT_METHOD_EXPIRED = "PAYMENT_008";
    public static final String DUPLICATE_PAYMENT_METHOD = "PAYMENT_009";
    public static final String UNAUTHORIZED_ACCESS = "PAYMENT_010";
}
