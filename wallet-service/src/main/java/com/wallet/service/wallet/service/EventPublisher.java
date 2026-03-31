package com.wallet.service.wallet.service;

import com.wallet.service.wallet.event.TransactionEvent;
import com.wallet.service.wallet.event.WalletEvent;

public interface EventPublisher {
    void publishTransactionEvent(TransactionEvent event);
    void publishWalletEvent(WalletEvent event);
}
