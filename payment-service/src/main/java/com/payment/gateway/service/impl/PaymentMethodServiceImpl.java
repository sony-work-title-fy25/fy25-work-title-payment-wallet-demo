package com.payment.gateway.service.impl;

import com.payment.gateway.dto.request.PaymentMethodRequest;
import com.payment.gateway.dto.response.PaymentMethodResponse;
import com.payment.gateway.entity.PaymentMethod;
import com.payment.gateway.exception.PaymentException;
import com.payment.gateway.exception.ResourceNotFoundException;
import com.payment.gateway.mapper.PaymentMapper;
import com.payment.gateway.repository.PaymentMethodRepository;
import com.payment.gateway.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentMethodResponse addPaymentMethod(PaymentMethodRequest request, String userId) {
        log.info("Adding payment method for user: {} type: {}", userId, request.getType());

        if (paymentMethodRepository.findByUserIdAndProviderToken(userId, request.getProviderToken()).isPresent()) {
            throw new PaymentException("This payment method is already registered", PaymentException.DUPLICATE_PAYMENT_METHOD);
        }

        if (request.isSetAsDefault()) {
            paymentMethodRepository.clearDefaultForUser(userId);
        } else if (!paymentMethodRepository.existsByUserIdAndIsActiveTrue(userId)) {
            request = PaymentMethodRequest.builder()
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
                    .setAsDefault(true)
                    .build();
        }

        PaymentMethod paymentMethod = paymentMapper.toPaymentMethod(request, userId);
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        log.info("Payment method added successfully. ID: {}", paymentMethod.getId());
        return paymentMapper.toPaymentMethodResponse(paymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getPaymentMethods(String userId) {
        log.debug("Getting payment methods for user: {}", userId);
        return paymentMethodRepository.findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(paymentMapper::toPaymentMethodResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponse getPaymentMethod(UUID paymentMethodId, String userId) {
        PaymentMethod paymentMethod = getPaymentMethodForUser(paymentMethodId, userId);
        return paymentMapper.toPaymentMethodResponse(paymentMethod);
    }

    @Override
    public void deletePaymentMethod(UUID paymentMethodId, String userId) {
        log.info("Deleting payment method: {} for user: {}", paymentMethodId, userId);

        PaymentMethod paymentMethod = getPaymentMethodForUser(paymentMethodId, userId);
        int updated = paymentMethodRepository.softDeleteByIdAndUserId(paymentMethodId, userId);
        
        if (updated == 0) {
            throw new ResourceNotFoundException("PaymentMethod", "id", paymentMethodId);
        }

        if (paymentMethod.isDefault()) {
            paymentMethodRepository.findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId)
                    .stream()
                    .findFirst()
                    .ifPresent(pm -> {
                        pm.setDefault(true);
                        paymentMethodRepository.save(pm);
                    });
        }
        log.info("Payment method deleted successfully: {}", paymentMethodId);
    }

    @Override
    public PaymentMethodResponse setDefaultPaymentMethod(UUID paymentMethodId, String userId) {
        log.info("Setting payment method {} as default for user: {}", paymentMethodId, userId);

        PaymentMethod paymentMethod = getPaymentMethodForUser(paymentMethodId, userId);
        paymentMethodRepository.clearDefaultForUser(userId);
        paymentMethod.setDefault(true);
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        log.info("Payment method set as default: {}", paymentMethodId);
        return paymentMapper.toPaymentMethodResponse(paymentMethod);
    }

    private PaymentMethod getPaymentMethodForUser(UUID paymentMethodId, String userId) {
        return paymentMethodRepository.findByIdAndUserId(paymentMethodId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", "id", paymentMethodId));
    }
}
