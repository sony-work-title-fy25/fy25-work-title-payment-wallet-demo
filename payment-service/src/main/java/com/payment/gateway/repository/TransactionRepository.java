package com.payment.gateway.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payment.gateway.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(UUID id, String userId);
}
