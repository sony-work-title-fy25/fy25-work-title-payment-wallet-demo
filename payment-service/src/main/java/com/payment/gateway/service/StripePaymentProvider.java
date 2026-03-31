package com.payment.gateway.service;

import com.payment.gateway.entity.Transaction;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class StripePaymentProvider {

    @Value("${stripe.api.key:sk_test_dummy}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe payment provider initialized");
    }

    public String processPayment(Transaction transaction) {
        log.info("Processing payment via Stripe for transaction: {}", transaction.getId());

        try {
            long amountInCents = transaction.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(transaction.getCurrency().toLowerCase())
                    .setDescription(transaction.getDescription())
                    .putMetadata("transaction_id", transaction.getId().toString())
                    .putMetadata("user_id", transaction.getUserId())
                    .putMetadata("product_id", transaction.getProductId() != null ? transaction.getProductId() : "")
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            log.info("Stripe payment intent created: {}", paymentIntent.getId());
            return paymentIntent.getId();

        } catch (StripeException e) {
            log.error("Stripe payment failed for transaction {}: {}", transaction.getId(), e.getMessage());
            throw new RuntimeException("Stripe payment failed: " + e.getMessage(), e);
        }
    }

    public String processRefund(Transaction transaction, BigDecimal amount) {
        log.info("Processing refund via Stripe for transaction: {} amount: {}", transaction.getId(), amount);

        try {
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(transaction.getExternalTransactionId())
                    .setAmount(amountInCents)
                    .putMetadata("transaction_id", transaction.getId().toString())
                    .putMetadata("user_id", transaction.getUserId())
                    .build();

            Refund refund = Refund.create(params);
            log.info("Stripe refund created: {}", refund.getId());
            return refund.getId();

        } catch (StripeException e) {
            log.error("Stripe refund failed for transaction {}: {}", transaction.getId(), e.getMessage());
            throw new RuntimeException("Stripe refund failed: " + e.getMessage(), e);
        }
    }
}
