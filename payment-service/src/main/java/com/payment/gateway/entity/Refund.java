package com.payment.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refund_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_refund_user_id", columnList = "userId"),
        @Index(name = "idx_refund_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundReason reason;

    @Column(length = 500)
    private String reasonDetails;

    @Column(length = 100)
    private String externalRefundId;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 100)
    private String processedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime processedAt;

    @Version
    private Long version;

    public void approve(String externalRefundId, String processedBy) {
        this.status = RefundStatus.COMPLETED;
        this.externalRefundId = externalRefundId;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(String reason, String processedBy) {
        this.status = RefundStatus.REJECTED;
        this.failureReason = reason;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
    }
}
