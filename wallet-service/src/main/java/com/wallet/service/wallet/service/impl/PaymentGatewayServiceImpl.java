package com.wallet.service.wallet.service.impl;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.wallet.service.wallet.exception.TransactionFailedException;
import com.wallet.service.wallet.service.PaymentGatewayService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    
    @Value("${payment.stripe.api-key}")
    private String stripeApiKey;
    
    @Value("${payment.paypal.client-id}")
    private String paypalClientId;
    
    @Value("${payment.paypal.client-secret}")
    private String paypalClientSecret;
    
    @Value("${payment.paypal.mode}")
    private String paypalMode;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
    
    @Override
    public String processPayment(BigDecimal amount, String paymentMethod, String gateway) {
        log.info("Processing payment through {}: amount={}", gateway, amount);
        
        if ("STRIPE".equalsIgnoreCase(gateway)) {
            return processStripePayment(amount);
        } else if ("PAYPAL".equalsIgnoreCase(gateway)) {
            return processPayPalPayment(amount);
        } else {
            throw new TransactionFailedException("Unsupported payment gateway: " + gateway);
        }
    }
    
    private String processStripePayment(BigDecimal amount) {
        try {
            // Convert amount to cents (Stripe requires smallest currency unit)
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .addPaymentMethodType("card")
                .setDescription("Wallet deposit")
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            log.info("Stripe payment intent created: {}", paymentIntent.getId());
            return paymentIntent.getId();
            
        } catch (StripeException e) {
            log.error("Stripe payment failed", e);
            throw new TransactionFailedException("Stripe payment failed: " + e.getMessage());
        }
    }
    
    private String processPayPalPayment(BigDecimal amount) {
        try {
            APIContext apiContext = new APIContext(paypalClientId, paypalClientSecret, paypalMode);
            
            Amount paypalAmount = new Amount();
            paypalAmount.setCurrency("USD");
            paypalAmount.setTotal(amount.toString());
            
            Transaction transaction = new Transaction();
            transaction.setDescription("Wallet deposit");
            transaction.setAmount(paypalAmount);
            
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);
            
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");
            
            Payment payment = new Payment();
            payment.setIntent("sale");
            payment.setPayer(payer);
            payment.setTransactions(transactions);
            
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl("http://localhost:8083/api/v1/payment/cancel");
            redirectUrls.setReturnUrl("http://localhost:8083/api/v1/payment/success");
            payment.setRedirectUrls(redirectUrls);
            
            Payment createdPayment = payment.create(apiContext);
            
            log.info("PayPal payment created: {}", createdPayment.getId());
            return createdPayment.getId();
            
        } catch (PayPalRESTException e) {
            log.error("PayPal payment failed", e);
            throw new TransactionFailedException("PayPal payment failed: " + e.getMessage());
        }
    }
    
    @Override
    public boolean verifyPayment(String referenceId) {
        log.info("Verifying payment: {}", referenceId);
        // Implementation depends on the payment gateway
        // For now, return true as a placeholder
        return true;
    }
    
    @Override
    public void refundPayment(String referenceId, BigDecimal amount) {
        log.info("Processing refund for payment: {}, amount: {}", referenceId, amount);
        
        try {
            // Attempt Stripe refund (you'd need to determine which gateway was used)
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
            
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(referenceId)
                .setAmount(amountInCents)
                .build();
            
            Refund refund = Refund.create(params);
            
            log.info("Refund processed: {}", refund.getId());
            
        } catch (StripeException e) {
            log.error("Refund failed", e);
            throw new TransactionFailedException("Refund failed: " + e.getMessage());
        }
    }
}
