package com.wallet.service.wallet.service.impl;

import com.wallet.service.wallet.event.TransactionEvent;
import com.wallet.service.wallet.event.WalletEvent;
import com.wallet.service.wallet.service.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "false")
public class NoOpEventPublisher implements EventPublisher {
    
    @Override
    public void publishTransactionEvent(TransactionEvent event) {
        log.debug("NoOp: Transaction event not published (Kafka disabled): {}", event.getTransactionId());
    }
    
    @Override
    public void publishWalletEvent(WalletEvent event) {
        log.debug("NoOp: Wallet event not published (Kafka disabled): {}", event.getWalletId());
    }
}
