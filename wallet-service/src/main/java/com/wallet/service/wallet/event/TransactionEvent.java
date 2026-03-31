package com.wallet.service.wallet.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent implements Serializable {
    private String transactionId;
    private String walletId;
    private String type;
    private BigDecimal amount;
    private String status;
    private String description;
    private LocalDateTime timestamp;
}
