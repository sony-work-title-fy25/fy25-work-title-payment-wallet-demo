package com.payment.gateway.entity;

public enum TransactionStatus {
    INITIATED,
    PROCESSING,
    PENDING_CONFIRMATION,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
