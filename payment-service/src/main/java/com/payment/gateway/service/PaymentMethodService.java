package com.payment.gateway.service;

import com.payment.gateway.dto.request.PaymentMethodRequest;
import com.payment.gateway.dto.response.PaymentMethodResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodService {

    PaymentMethodResponse addPaymentMethod(PaymentMethodRequest request, String userId);

    List<PaymentMethodResponse> getPaymentMethods(String userId);

    PaymentMethodResponse getPaymentMethod(UUID paymentMethodId, String userId);

    void deletePaymentMethod(UUID paymentMethodId, String userId);

    PaymentMethodResponse setDefaultPaymentMethod(UUID paymentMethodId, String userId);
}
