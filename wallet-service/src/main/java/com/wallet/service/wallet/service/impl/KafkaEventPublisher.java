package com.wallet.service.wallet.service.impl;

import com.wallet.service.wallet.event.TransactionEvent;
import com.wallet.service.wallet.event.WalletEvent;
import com.wallet.service.wallet.service.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaEventPublisher implements EventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.transaction-events}")
    private String transactionTopic;
    
    @Value("${kafka.topics.wallet-events}")
    private String walletTopic;
    
    @Override
    public void publishTransactionEvent(TransactionEvent event) {
        log.info("Publishing transaction event: {}", event.getTransactionId());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(transactionTopic, event.getTransactionId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Transaction event published successfully: {}", event.getTransactionId());
            } else {
                log.error("Failed to publish transaction event: {}", event.getTransactionId(), ex);
            }
        });
    }
    
    @Override
    public void publishWalletEvent(WalletEvent event) {
        log.info("Publishing wallet event: {}", event.getWalletId());
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(walletTopic, event.getWalletId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Wallet event published successfully: {}", event.getWalletId());
            } else {
                log.error("Failed to publish wallet event: {}", event.getWalletId(), ex);
            }
        });
    }
}
