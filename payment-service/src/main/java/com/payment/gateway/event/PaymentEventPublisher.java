package com.payment.gateway.event;

import com.payment.gateway.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final boolean kafkaEnabled;

    private static final String PAYMENT_TOPIC = "payment-events";

    @Autowired(required = false)
    public PaymentEventPublisher(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaEnabled = kafkaTemplate != null;
        if (!kafkaEnabled) {
            log.info("Kafka is disabled - events will be logged only");
        }
    }

    public PaymentEventPublisher() {
        this.kafkaTemplate = null;
        this.kafkaEnabled = false;
        log.info("Kafka is disabled - events will be logged only");
    }

    public void publishPaymentInitiated(Transaction transaction) {
        PaymentEvent event = buildEvent(transaction, PaymentEvent.PAYMENT_INITIATED);
        publishEvent(event);
        log.info("Published payment.initiated event for transaction: {}", transaction.getId());
    }

    public void publishPaymentCompleted(Transaction transaction) {
        PaymentEvent event = buildEvent(transaction, PaymentEvent.PAYMENT_COMPLETED);
        publishEvent(event);
        log.info("Published payment.completed event for transaction: {}", transaction.getId());
    }

    public void publishPaymentFailed(Transaction transaction, String reason) {
        PaymentEvent event = buildEvent(transaction, PaymentEvent.PAYMENT_FAILED);
        event.setFailureReason(reason);
        publishEvent(event);
        log.info("Published payment.failed event for transaction: {}", transaction.getId());
    }

    public void publishPaymentRefunded(Transaction transaction) {
        PaymentEvent event = buildEvent(transaction, PaymentEvent.PAYMENT_REFUNDED);
        publishEvent(event);
        log.info("Published payment.refunded event for transaction: {}", transaction.getId());
    }

    public void publishPaymentCancelled(Transaction transaction) {
        PaymentEvent event = buildEvent(transaction, PaymentEvent.PAYMENT_CANCELLED);
        publishEvent(event);
        log.info("Published payment.cancelled event for transaction: {}", transaction.getId());
    }

    private PaymentEvent buildEvent(Transaction transaction, String eventType) {
        return PaymentEvent.builder()
                .eventType(eventType)
                .transactionId(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .paymentType(transaction.getPaymentType().name())
                .status(transaction.getStatus().name())
                .productId(transaction.getProductId())
                .productType(transaction.getProductType())
                .platform(transaction.getPlatform())
                .region(transaction.getRegion())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void publishEvent(PaymentEvent event) {
        if (!kafkaEnabled) {
            log.debug("[DEV MODE] Would publish event: {} for transaction: {}", 
                    event.getEventType(), event.getTransactionId());
            return;
        }

        CompletableFuture<SendResult<String, PaymentEvent>> future =
                kafkaTemplate.send(PAYMENT_TOPIC, event.getTransactionId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} for transaction {}: {}",
                        event.getEventType(), event.getTransactionId(), ex.getMessage());
            } else {
                log.debug("Event {} published successfully for transaction {} at offset {}",
                        event.getEventType(), event.getTransactionId(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
