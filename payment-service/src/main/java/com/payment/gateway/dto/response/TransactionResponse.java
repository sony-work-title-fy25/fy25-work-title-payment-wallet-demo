package com.payment.gateway.dto.response;

import com.payment.gateway.entity.PaymentType;
import com.payment.gateway.entity.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private UUID id;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private PaymentType paymentType;
    private String description;
    private String merchantReference;
    private String externalTransactionId;
    private String paymentProvider;
    private PaymentMethodSummary paymentMethod;
    private String productId;
    private String productType;
    private String platform;
    private String region;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethodSummary {
        private UUID id;
        private String type;
        private String displayName;
    }
}
