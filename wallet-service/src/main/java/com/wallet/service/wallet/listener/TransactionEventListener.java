package com.wallet.service.wallet.listener;

import com.wallet.service.wallet.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class TransactionEventListener {
    
    @KafkaListener(
        topics = "${kafka.topics.transaction-events}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleTransactionEvent(TransactionEvent event) {
        log.info("Received transaction event: {}", event);
        
        // Process transaction event
        // Could be used for:
        // - Sending notifications
        // - Updating analytics
        // - Triggering business rules
        // - Audit logging
        
        log.info("Transaction event processed successfully: {}", event.getTransactionId());
    }
}
