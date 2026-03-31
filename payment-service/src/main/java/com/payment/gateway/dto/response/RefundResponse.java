package com.payment.gateway.dto.response;

import com.payment.gateway.entity.RefundReason;
import com.payment.gateway.entity.RefundStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {

    private UUID id;
    private UUID transactionId;
    private BigDecimal amount;
    private String currency;
    private RefundStatus status;
    private RefundReason reason;
    private String reasonDetails;
    private String externalRefundId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
