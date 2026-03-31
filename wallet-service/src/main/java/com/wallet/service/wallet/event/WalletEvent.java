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
public class WalletEvent implements Serializable {
    private String walletId;
    private String eventType; // BALANCE_UPDATED, WALLET_CREATED, WALLET_FROZEN, etc.
    private BigDecimal newBalance;
    private BigDecimal oldBalance;
    private LocalDateTime timestamp;
}
