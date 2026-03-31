package com.payment.gateway.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.payment.gateway.entity.Refund;
import com.payment.gateway.entity.RefundStatus;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.transaction.id = :transactionId AND r.status = 'COMPLETED'")
    BigDecimal getTotalRefundedAmountForTransaction(@Param("transactionId") UUID transactionId);

    boolean existsByTransactionIdAndStatusIn(UUID transactionId, List<RefundStatus> statuses);
}
