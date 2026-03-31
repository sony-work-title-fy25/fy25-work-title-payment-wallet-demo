package com.wallet.service.wallet.exception;

public class TransactionFailedException extends WalletException {
    public TransactionFailedException(String message) {
        super(message);
    }
    
    public TransactionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
