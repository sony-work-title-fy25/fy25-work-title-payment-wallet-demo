package com.wallet.service.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_id", columnList = "transactionId"),
    @Index(name = "idx_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String transactionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal balanceAfter;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    private String description;
    
    private String referenceId; // External payment gateway reference
    
    // For wallet-to-wallet transfers
    private Long sourceWalletId;
    private Long targetWalletId;
    
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    private String paymentGateway; // Stripe, PayPal, etc.
    
    private String failureReason;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data
    
    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        PAYMENT,
        TRANSFER_OUT,
        TRANSFER_IN,
        REFUND,
        ADJUSTMENT
    }
    
    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED
    }
    
    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        BANK_TRANSFER,
        WALLET_BALANCE,
        PAYPAL,
        STRIPE
    }
}
