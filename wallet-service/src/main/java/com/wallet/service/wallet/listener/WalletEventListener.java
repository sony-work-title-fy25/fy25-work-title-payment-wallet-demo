package com.wallet.service.wallet.listener;

import com.wallet.service.wallet.event.WalletEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class WalletEventListener {
    
    @KafkaListener(
        topics = "${kafka.topics.wallet-events}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleWalletEvent(WalletEvent event) {
        log.info("Received wallet event: {}", event);
        
        // Process wallet event
        // Could be used for:
        // - Real-time balance updates
        // - Fraud detection
        // - Balance threshold alerts
        // - Syncing with external systems
        
        log.info("Wallet event processed successfully: {}", event.getWalletId());
    }
}
