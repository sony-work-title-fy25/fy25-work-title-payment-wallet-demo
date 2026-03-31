package com.payment.gateway.service;

import com.payment.gateway.dto.request.PaymentInitiateRequest;
import com.payment.gateway.dto.request.RefundRequest;
import com.payment.gateway.dto.response.PagedResponse;
import com.payment.gateway.dto.response.RefundResponse;
import com.payment.gateway.dto.response.TransactionResponse;

import java.util.UUID;

public interface PaymentService {

    TransactionResponse initiatePayment(PaymentInitiateRequest request, String userId);

    TransactionResponse confirmPayment(UUID transactionId, String userId);

    TransactionResponse cancelPayment(UUID transactionId, String userId);

    TransactionResponse getTransaction(UUID transactionId, String userId);

    PagedResponse<TransactionResponse> getTransactionHistory(String userId, int page, int size);

    RefundResponse initiateRefund(UUID transactionId, RefundRequest request, String userId);
}
