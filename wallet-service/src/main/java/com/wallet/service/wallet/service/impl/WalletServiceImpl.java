package com.wallet.service.wallet.service.impl;

import com.wallet.service.wallet.dto.*;
import com.wallet.service.wallet.entity.Transaction;
import com.wallet.service.wallet.entity.Wallet;
import com.wallet.service.wallet.event.TransactionEvent;
import com.wallet.service.wallet.event.WalletEvent;
import com.wallet.service.wallet.exception.InsufficientBalanceException;
import com.wallet.service.wallet.exception.TransactionFailedException;
import com.wallet.service.wallet.exception.WalletNotFoundException;
import com.wallet.service.wallet.repository.TransactionRepository;
import com.wallet.service.wallet.repository.WalletRepository;
import com.wallet.service.wallet.service.EventPublisher;
import com.wallet.service.wallet.service.PaymentGatewayService;
import com.wallet.service.wallet.service.WalletService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {
    
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final EventPublisher eventPublisher;
    private final PaymentGatewayService paymentGatewayService;
    private final MeterRegistry meterRegistry;
    
    @Override
    @Cacheable(value = "walletBalance", key = "#userEmail")
    public WalletBalanceResponse getBalance(String userEmail) {
        log.info("Fetching balance for user: {}", userEmail);
        Wallet wallet = getWalletByUserEmail(userEmail);
        
        return WalletBalanceResponse.builder()
            .walletId(wallet.getWalletId())
            .balance(wallet.getBalance())
            .currency(wallet.getCurrency())
            .status(wallet.getStatus().name())
            .build();
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "walletBalance", key = "#userEmail")
    public TransactionResponse deposit(String userEmail, DepositRequest request) {
        log.info("Processing deposit for user: {}, amount: {}", userEmail, request.getAmount());
        
        Wallet wallet = getWalletByUserEmailWithLock(userEmail);
        
        // Process payment through payment gateway
        String paymentReference = null;
        try {
            if (request.getPaymentGateway() != null) {
                paymentReference = paymentGatewayService.processPayment(
                    request.getAmount(),
                    request.getPaymentMethod(),
                    request.getPaymentGateway()
                );
            }
        } catch (Exception e) {
            log.error("Payment gateway processing failed", e);
            throw new TransactionFailedException("Payment processing failed: " + e.getMessage());
        }
        
        // Create transaction
        Transaction transaction = createTransaction(
            wallet,
            Transaction.TransactionType.DEPOSIT,
            request.getAmount(),
            request.getDescription(),
            paymentReference
        );
        transaction.setPaymentMethod(Transaction.PaymentMethod.valueOf(request.getPaymentMethod()));
        transaction.setPaymentGateway(request.getPaymentGateway());
        
        // Update wallet balance
        BigDecimal oldBalance = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        
        walletRepository.save(wallet);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Publish events
        publishTransactionEvent(savedTransaction);
        publishWalletEvent(wallet, oldBalance);
        
        // Metrics
        incrementCounter("wallet.deposit.success");
        
        log.info("Deposit completed successfully. Transaction ID: {}", savedTransaction.getTransactionId());
        return TransactionResponse.fromEntity(savedTransaction);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "walletBalance", key = "#userEmail")
    public TransactionResponse withdraw(String userEmail, WithdrawRequest request) {
        log.info("Processing withdrawal for user: {}, amount: {}", userEmail, request.getAmount());
        
        Wallet wallet = getWalletByUserEmailWithLock(userEmail);
        
        // Check sufficient balance
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            incrementCounter("wallet.withdraw.insufficient_balance");
            throw new InsufficientBalanceException("Insufficient balance. Available: " + wallet.getBalance());
        }
        
        // Create transaction
        Transaction transaction = createTransaction(
            wallet,
            Transaction.TransactionType.WITHDRAWAL,
            request.getAmount(),
            request.getDescription(),
            null
        );
        transaction.setPaymentMethod(Transaction.PaymentMethod.valueOf(request.getPaymentMethod()));
        
        // Update wallet balance
        BigDecimal oldBalance = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        
        walletRepository.save(wallet);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Publish events
        publishTransactionEvent(savedTransaction);
        publishWalletEvent(wallet, oldBalance);
        
        // Metrics
        incrementCounter("wallet.withdraw.success");
        
        log.info("Withdrawal completed successfully. Transaction ID: {}", savedTransaction.getTransactionId());
        return TransactionResponse.fromEntity(savedTransaction);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "walletBalance", key = "#userEmail")
    public TransactionResponse pay(String userEmail, PaymentRequest request) {
        log.info("Processing payment for user: {}, amount: {}, merchant: {}", 
            userEmail, request.getAmount(), request.getMerchantId());
        
        Wallet wallet = getWalletByUserEmailWithLock(userEmail);
        
        // Check sufficient balance
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            incrementCounter("wallet.payment.insufficient_balance");
            throw new InsufficientBalanceException("Insufficient balance. Available: " + wallet.getBalance());
        }
        
        // Create transaction
        Transaction transaction = createTransaction(
            wallet,
            Transaction.TransactionType.PAYMENT,
            request.getAmount(),
            request.getDescription(),
            request.getOrderId()
        );
        transaction.setPaymentMethod(Transaction.PaymentMethod.WALLET_BALANCE);
        
        // Update wallet balance
        BigDecimal oldBalance = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        
        walletRepository.save(wallet);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Publish events
        publishTransactionEvent(savedTransaction);
        publishWalletEvent(wallet, oldBalance);
        
        // Metrics
        incrementCounter("wallet.payment.success");
        
        log.info("Payment completed successfully. Transaction ID: {}", savedTransaction.getTransactionId());
        return TransactionResponse.fromEntity(savedTransaction);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "walletBalance", allEntries = true)
    public TransactionResponse transfer(String userEmail, TransferRequest request) {
        log.info("Processing transfer from user: {} to wallet: {}, amount: {}", 
            userEmail, request.getTargetWalletId(), request.getAmount());
        
        // Get source wallet with lock
        Wallet sourceWallet = getWalletByUserEmailWithLock(userEmail);
        
        // Get target wallet with lock
        Wallet targetWallet = walletRepository.findByWalletIdWithLock(request.getTargetWalletId())
            .orElseThrow(() -> new WalletNotFoundException("Target wallet not found"));
        
        // Check sufficient balance
        if (sourceWallet.getBalance().compareTo(request.getAmount()) < 0) {
            incrementCounter("wallet.transfer.insufficient_balance");
            throw new InsufficientBalanceException("Insufficient balance. Available: " + sourceWallet.getBalance());
        }
        
        // Create transfer-out transaction
        Transaction transferOut = createTransaction(
            sourceWallet,
            Transaction.TransactionType.TRANSFER_OUT,
            request.getAmount(),
            request.getDescription(),
            null
        );
        transferOut.setSourceWalletId(sourceWallet.getId());
        transferOut.setTargetWalletId(targetWallet.getId());
        transferOut.setPaymentMethod(Transaction.PaymentMethod.WALLET_BALANCE);
        
        // Update source wallet balance
        BigDecimal sourceOldBalance = sourceWallet.getBalance();
        sourceWallet.setBalance(sourceWallet.getBalance().subtract(request.getAmount()));
        transferOut.setBalanceAfter(sourceWallet.getBalance());
        transferOut.setStatus(Transaction.TransactionStatus.COMPLETED);
        transferOut.setCompletedAt(LocalDateTime.now());
        
        // Create transfer-in transaction
        Transaction transferIn = createTransaction(
            targetWallet,
            Transaction.TransactionType.TRANSFER_IN,
            request.getAmount(),
            request.getDescription(),
            null
        );
        transferIn.setSourceWalletId(sourceWallet.getId());
        transferIn.setTargetWalletId(targetWallet.getId());
        transferIn.setPaymentMethod(Transaction.PaymentMethod.WALLET_BALANCE);
        
        // Update target wallet balance
        BigDecimal targetOldBalance = targetWallet.getBalance();
        targetWallet.setBalance(targetWallet.getBalance().add(request.getAmount()));
        transferIn.setBalanceAfter(targetWallet.getBalance());
        transferIn.setStatus(Transaction.TransactionStatus.COMPLETED);
        transferIn.setCompletedAt(LocalDateTime.now());
        
        // Save everything
        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);
        Transaction savedTransferOut = transactionRepository.save(transferOut);
        transactionRepository.save(transferIn);
        
        // Publish events
        publishTransactionEvent(savedTransferOut);
        publishWalletEvent(sourceWallet, sourceOldBalance);
        publishWalletEvent(targetWallet, targetOldBalance);
        
        // Metrics
        incrementCounter("wallet.transfer.success");
        
        log.info("Transfer completed successfully. Transaction ID: {}", savedTransferOut.getTransactionId());
        return TransactionResponse.fromEntity(savedTransferOut);
    }
    
    @Override
    @Cacheable(value = "transactions", key = "#userEmail")
    public List<TransactionResponse> getTransactions(String userEmail) {
        log.info("Fetching transactions for user: {}", userEmail);
        Wallet wallet = getWalletByUserEmail(userEmail);
        
        List<Transaction> transactions = transactionRepository
            .findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        
        return transactions.stream()
            .map(TransactionResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<TransactionResponse> getTransactionsPaginated(String userEmail, Pageable pageable) {
        log.info("Fetching paginated transactions for user: {}", userEmail);
        Wallet wallet = getWalletByUserEmail(userEmail);
        
        Page<Transaction> transactions = transactionRepository.findByWalletId(wallet.getId(), pageable);
        
        return transactions.map(TransactionResponse::fromEntity);
    }
    
    // Helper methods
    
    private Wallet getWalletByUserEmail(String email) {
        return walletRepository.findByUserEmail(email)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + email));
    }
    
    private Wallet getWalletByUserEmailWithLock(String email) {
        Wallet wallet = getWalletByUserEmail(email);
        return walletRepository.findByIdWithLock(wallet.getId())
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + email));
    }
    
    private Transaction createTransaction(
        Wallet wallet,
        Transaction.TransactionType type,
        BigDecimal amount,
        String description,
        String referenceId
    ) {
        return Transaction.builder()
            .transactionId(UUID.randomUUID().toString())
            .wallet(wallet)
            .type(type)
            .amount(amount)
            .balanceBefore(wallet.getBalance())
            .status(Transaction.TransactionStatus.PENDING)
            .description(description)
            .referenceId(referenceId)
            .currency(wallet.getCurrency())
            .build();
    }
    
    private void publishTransactionEvent(Transaction transaction) {
        try {
            TransactionEvent event = TransactionEvent.builder()
                .transactionId(transaction.getTransactionId())
                .walletId(transaction.getWallet().getWalletId())
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
            
            eventPublisher.publishTransactionEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish transaction event", e);
        }
    }
    
    private void publishWalletEvent(Wallet wallet, BigDecimal oldBalance) {
        try {
            WalletEvent event = WalletEvent.builder()
                .walletId(wallet.getWalletId())
                .eventType("BALANCE_UPDATED")
                .newBalance(wallet.getBalance())
                .oldBalance(oldBalance)
                .timestamp(LocalDateTime.now())
                .build();
            
            eventPublisher.publishWalletEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish wallet event", e);
        }
    }
    
    private void incrementCounter(String metricName) {
        Counter.builder(metricName)
            .register(meterRegistry)
            .increment();
    }
}
