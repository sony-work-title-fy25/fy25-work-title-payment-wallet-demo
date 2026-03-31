package com.payment.gateway.mapper;

import com.payment.gateway.dto.request.PaymentInitiateRequest;
import com.payment.gateway.dto.request.PaymentMethodRequest;
import com.payment.gateway.dto.response.PaymentMethodResponse;
import com.payment.gateway.dto.response.RefundResponse;
import com.payment.gateway.dto.response.TransactionResponse;
import com.payment.gateway.entity.PaymentMethod;
import com.payment.gateway.entity.Refund;
import com.payment.gateway.entity.Transaction;
import com.payment.gateway.entity.TransactionStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Transaction toTransaction(PaymentInitiateRequest request, String userId) {
        return Transaction.builder()
                .userId(userId)
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .paymentType(request.getPaymentType())
                .description(request.getDescription())
                .merchantReference(request.getMerchantReference())
                .productId(request.getProductId())
                .productType(request.getProductType())
                .platform(request.getPlatform())
                .region(request.getRegion())
                .status(TransactionStatus.INITIATED)
                .build();
    }

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        TransactionResponse.PaymentMethodSummary pmSummary = null;
        if (transaction.getPaymentMethod() != null) {
            PaymentMethod pm = transaction.getPaymentMethod();
            pmSummary = TransactionResponse.PaymentMethodSummary.builder()
                    .id(pm.getId())
                    .type(pm.getType().name())
                    .displayName(pm.getDisplayName())
                    .build();
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .paymentType(transaction.getPaymentType())
                .description(transaction.getDescription())
                .merchantReference(transaction.getMerchantReference())
                .externalTransactionId(transaction.getExternalTransactionId())
                .paymentProvider(transaction.getPaymentProvider())
                .paymentMethod(pmSummary)
                .productId(transaction.getProductId())
                .productType(transaction.getProductType())
                .platform(transaction.getPlatform())
                .region(transaction.getRegion())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }

    public PaymentMethod toPaymentMethod(PaymentMethodRequest request, String userId) {
        return PaymentMethod.builder()
                .userId(userId)
                .type(request.getType())
                .nickname(request.getNickname())
                .providerToken(request.getProviderToken())
                .lastFourDigits(request.getLastFourDigits())
                .cardBrand(request.getCardBrand())
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .billingName(request.getBillingName())
                .billingAddress(request.getBillingAddress())
                .billingCity(request.getBillingCity())
                .billingState(request.getBillingState())
                .billingPostalCode(request.getBillingPostalCode())
                .billingCountry(request.getBillingCountry())
                .isDefault(request.isSetAsDefault())
                .isActive(true)
                .isVerified(false)
                .build();
    }

    public PaymentMethodResponse toPaymentMethodResponse(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .type(paymentMethod.getType())
                .nickname(paymentMethod.getNickname())
                .displayName(paymentMethod.getDisplayName())
                .lastFourDigits(paymentMethod.getLastFourDigits())
                .cardBrand(paymentMethod.getCardBrand())
                .expiryMonth(paymentMethod.getExpiryMonth())
                .expiryYear(paymentMethod.getExpiryYear())
                .billingName(paymentMethod.getBillingName())
                .billingCountry(paymentMethod.getBillingCountry())
                .isDefault(paymentMethod.isDefault())
                .isActive(paymentMethod.isActive())
                .isVerified(paymentMethod.isVerified())
                .isExpired(paymentMethod.isExpired())
                .createdAt(paymentMethod.getCreatedAt())
                .build();
    }

    public RefundResponse toRefundResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .transactionId(refund.getTransaction().getId())
                .amount(refund.getAmount())
                .currency(refund.getCurrency())
                .status(refund.getStatus())
                .reason(refund.getReason())
                .reasonDetails(refund.getReasonDetails())
                .externalRefundId(refund.getExternalRefundId())
                .createdAt(refund.getCreatedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }
}
