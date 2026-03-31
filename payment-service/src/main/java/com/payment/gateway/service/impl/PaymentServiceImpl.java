package com.payment.gateway.service.impl;

import com.payment.gateway.dto.request.PaymentInitiateRequest;
import com.payment.gateway.dto.request.RefundRequest;
import com.payment.gateway.dto.response.PagedResponse;
import com.payment.gateway.dto.response.RefundResponse;
import com.payment.gateway.dto.response.TransactionResponse;
import com.payment.gateway.entity.*;
import com.payment.gateway.event.PaymentEventPublisher;
import com.payment.gateway.exception.PaymentException;
import com.payment.gateway.exception.ResourceNotFoundException;
import com.payment.gateway.mapper.PaymentMapper;
import com.payment.gateway.repository.PaymentMethodRepository;
import com.payment.gateway.repository.RefundRepository;
import com.payment.gateway.repository.TransactionRepository;
import com.payment.gateway.service.PaymentService;
import com.payment.gateway.service.StripePaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final RefundRepository refundRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentEventPublisher eventPublisher;
    private final StripePaymentProvider stripePaymentProvider;

    @Override
    public TransactionResponse initiatePayment(PaymentInitiateRequest request, String userId) {
        log.info("Initiating payment for user: {} amount: {} {}", userId, request.getAmount(), request.getCurrency());

        Transaction transaction = paymentMapper.toTransaction(request, userId);
        transaction.setPaymentProvider("STRIPE");

        if (request.getPaymentMethodId() != null) {
            PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUserId(request.getPaymentMethodId(), userId)
                    .orElseThrow(() -> new PaymentException("Payment method not found", PaymentException.PAYMENT_METHOD_NOT_FOUND));

            if (paymentMethod.isExpired()) {
                throw new PaymentException("Payment method has expired", PaymentException.PAYMENT_METHOD_EXPIRED);
            }
            transaction.setPaymentMethod(paymentMethod);
        }

        transaction = transactionRepository.save(transaction);
        eventPublisher.publishPaymentInitiated(transaction);

        log.info("Payment initiated successfully. Transaction ID: {}", transaction.getId());
        return paymentMapper.toTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse confirmPayment(UUID transactionId, String userId) {
        log.info("Confirming payment. Transaction ID: {}", transactionId);

        Transaction transaction = getTransactionForUser(transactionId, userId);

        if (transaction.getStatus() != TransactionStatus.INITIATED && transaction.getStatus() != TransactionStatus.PENDING_CONFIRMATION) {
            throw new PaymentException("Transaction cannot be confirmed in current state: " + transaction.getStatus(), PaymentException.INVALID_PAYMENT_STATE);
        }

        try {
            transaction.setStatus(TransactionStatus.PROCESSING);
            transactionRepository.save(transaction);

            String externalId = stripePaymentProvider.processPayment(transaction);
            transaction.complete(externalId);
            transaction = transactionRepository.save(transaction);

            eventPublisher.publishPaymentCompleted(transaction);
            log.info("Payment confirmed successfully. Transaction ID: {}", transactionId);
            return paymentMapper.toTransactionResponse(transaction);

        } catch (Exception e) {
            log.error("Payment processing failed for transaction {}: {}", transactionId, e.getMessage());
            transaction.fail(e.getMessage());
            transactionRepository.save(transaction);
            eventPublisher.publishPaymentFailed(transaction, e.getMessage());
            throw new PaymentException("Payment processing failed: " + e.getMessage(), PaymentException.PAYMENT_PROCESSING_FAILED, e);
        }
    }

    @Override
    public TransactionResponse cancelPayment(UUID transactionId, String userId) {
        log.info("Cancelling payment. Transaction ID: {}", transactionId);

        Transaction transaction = getTransactionForUser(transactionId, userId);

        if (transaction.getStatus() != TransactionStatus.INITIATED && transaction.getStatus() != TransactionStatus.PENDING_CONFIRMATION) {
            throw new PaymentException("Transaction cannot be cancelled in current state: " + transaction.getStatus(), PaymentException.INVALID_PAYMENT_STATE);
        }

        transaction.cancel();
        transaction = transactionRepository.save(transaction);
        eventPublisher.publishPaymentCancelled(transaction);
        log.info("Payment cancelled successfully. Transaction ID: {}", transactionId);
        return paymentMapper.toTransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID transactionId, String userId) {
        Transaction transaction = getTransactionForUser(transactionId, userId);
        return paymentMapper.toTransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactionHistory(String userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactionPage = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        List<TransactionResponse> content = transactionPage.getContent().stream().map(paymentMapper::toTransactionResponse).toList();

        return PagedResponse.<TransactionResponse>builder()
                .content(content)
                .page(transactionPage.getNumber())
                .size(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .first(transactionPage.isFirst())
                .last(transactionPage.isLast())
                .hasNext(transactionPage.hasNext())
                .hasPrevious(transactionPage.hasPrevious())
                .build();
    }

    @Override
    public RefundResponse initiateRefund(UUID transactionId, RefundRequest request, String userId) {
        log.info("Initiating refund for transaction: {}", transactionId);

        Transaction transaction = getTransactionForUser(transactionId, userId);

        if (transaction.getStatus() != TransactionStatus.COMPLETED && transaction.getStatus() != TransactionStatus.PARTIALLY_REFUNDED) {
            throw new PaymentException("Transaction cannot be refunded in current state: " + transaction.getStatus(), PaymentException.REFUND_NOT_ALLOWED);
        }

        if (refundRepository.existsByTransactionIdAndStatusIn(transactionId, List.of(RefundStatus.PENDING, RefundStatus.PROCESSING))) {
            throw new PaymentException("A refund is already pending for this transaction", PaymentException.REFUND_NOT_ALLOWED);
        }

        BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : transaction.getAmount();
        BigDecimal totalRefunded = refundRepository.getTotalRefundedAmountForTransaction(transactionId);
        BigDecimal maxRefundable = transaction.getAmount().subtract(totalRefunded);

        if (refundAmount.compareTo(maxRefundable) > 0) {
            throw new PaymentException("Refund amount exceeds maximum refundable amount: " + maxRefundable, PaymentException.REFUND_AMOUNT_EXCEEDED);
        }

        Refund refund = Refund.builder()
                .transaction(transaction)
                .userId(userId)
                .amount(refundAmount)
                .currency(transaction.getCurrency())
                .status(RefundStatus.PENDING)
                .reason(request.getReason())
                .reasonDetails(request.getReasonDetails())
                .build();

        refund = refundRepository.save(refund);

        try {
            String externalRefundId = stripePaymentProvider.processRefund(transaction, refundAmount);
            refund.approve(externalRefundId, "SYSTEM");
            refund = refundRepository.save(refund);

            if (refundAmount.compareTo(maxRefundable) == 0) {
                transaction.setStatus(TransactionStatus.REFUNDED);
            } else {
                transaction.setStatus(TransactionStatus.PARTIALLY_REFUNDED);
            }
            transactionRepository.save(transaction);

            eventPublisher.publishPaymentRefunded(transaction);

        } catch (Exception e) {
            log.error("Refund processing failed: {}", e.getMessage());
            refund.reject(e.getMessage(), "SYSTEM");
            refundRepository.save(refund);
            throw new PaymentException("Refund processing failed: " + e.getMessage(), PaymentException.PAYMENT_PROCESSING_FAILED, e);
        }
        log.info("Refund initiated successfully. Refund ID: {}", refund.getId());
        return paymentMapper.toRefundResponse(refund);
    }

    private Transaction getTransactionForUser(UUID transactionId, String userId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
    }
}
