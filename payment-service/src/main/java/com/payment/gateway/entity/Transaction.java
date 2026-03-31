package com.payment.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_user_id", columnList = "userId"),
        @Index(name = "idx_transaction_status", columnList = "status"),
        @Index(name = "idx_transaction_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentType paymentType;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String merchantReference;

    @Column(length = 100)
    private String externalTransactionId;

    @Column(length = 50)
    private String paymentProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 100)
    private String productId;

    @Column(length = 50)
    private String productType;

    @Column(length = 50)
    private String platform;

    @Column(length = 10)
    private String region;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime completedAt;

    @Version
    private Long version;

    public void complete(String externalTransactionId) {
        this.status = TransactionStatus.COMPLETED;
        this.externalTransactionId = externalTransactionId;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }

    public void cancel() {
        this.status = TransactionStatus.CANCELLED;
    }
}
