package com.payment.gateway.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {

    private String eventType;
    private UUID transactionId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String paymentType;
    private String status;
    private String productId;
    private String productType;
    private String platform;
    private String region;
    private String failureReason;
    private LocalDateTime timestamp;

    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_REFUNDED = "payment.refunded";
    public static final String PAYMENT_CANCELLED = "payment.cancelled";
}
