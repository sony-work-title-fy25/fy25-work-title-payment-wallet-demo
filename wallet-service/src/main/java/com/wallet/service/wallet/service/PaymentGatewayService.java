package com.wallet.service.wallet.service;

import java.math.BigDecimal;

public interface PaymentGatewayService {
    String processPayment(BigDecimal amount, String paymentMethod, String gateway);
    boolean verifyPayment(String referenceId);
    void refundPayment(String referenceId, BigDecimal amount);
}
