package com.wallet.service.wallet.dto;

import com.wallet.service.wallet.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String status;
    private String description;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String referenceId;
    
    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
            .transactionId(transaction.getTransactionId())
            .type(transaction.getType().name())
            .amount(transaction.getAmount())
            .balanceBefore(transaction.getBalanceBefore())
            .balanceAfter(transaction.getBalanceAfter())
            .status(transaction.getStatus().name())
            .description(transaction.getDescription())
            .currency(transaction.getCurrency())
            .createdAt(transaction.getCreatedAt())
            .completedAt(transaction.getCompletedAt())
            .referenceId(transaction.getReferenceId())
            .build();
    }
}
