package com.wallet.service.wallet.service;

import com.wallet.service.wallet.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WalletService {
    
    WalletBalanceResponse getBalance(String userEmail);
    
    TransactionResponse deposit(String userEmail, DepositRequest request);
    
    TransactionResponse withdraw(String userEmail, WithdrawRequest request);
    
    TransactionResponse pay(String userEmail, PaymentRequest request);
    
    TransactionResponse transfer(String userEmail, TransferRequest request);
    
    List<TransactionResponse> getTransactions(String userEmail);
    
    Page<TransactionResponse> getTransactionsPaginated(String userEmail, Pageable pageable);
}
