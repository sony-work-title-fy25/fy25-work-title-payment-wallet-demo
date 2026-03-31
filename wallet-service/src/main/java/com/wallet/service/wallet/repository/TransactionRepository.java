package com.wallet.service.wallet.repository;

import com.wallet.service.wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(@Param("walletId") Long walletId);
    
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByWalletIdAndDateRange(
        @Param("walletId") Long walletId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.wallet.id = :walletId AND t.type = :type AND t.status = 'COMPLETED' AND t.createdAt >= :startDate")
    BigDecimal sumAmountByWalletIdAndTypeAndDateAfter(
        @Param("walletId") Long walletId,
        @Param("type") Transaction.TransactionType type,
        @Param("startDate") LocalDateTime startDate
    );
    
    List<Transaction> findByReferenceId(String referenceId);
    
    @Query("SELECT t FROM Transaction t WHERE (t.sourceWalletId = :walletId OR t.targetWalletId = :walletId) AND t.type IN ('TRANSFER_IN', 'TRANSFER_OUT')")
    List<Transaction> findTransfersByWalletId(@Param("walletId") Long walletId);
}
