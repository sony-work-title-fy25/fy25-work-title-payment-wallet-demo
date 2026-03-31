package com.payment.gateway.entity;

public enum RefundReason {
    CUSTOMER_REQUEST,
    DUPLICATE_CHARGE,
    TECHNICAL_ISSUES,
    SUBSCRIPTION_CANCELLED,
    FRAUD,
    OTHER
}
