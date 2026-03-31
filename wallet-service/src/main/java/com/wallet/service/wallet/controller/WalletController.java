package com.wallet.service.wallet.controller;

import com.wallet.service.wallet.dto.ApiResponse;
import com.wallet.service.wallet.dto.DepositRequest;
import com.wallet.service.wallet.dto.PaymentRequest;
import com.wallet.service.wallet.dto.TransactionResponse;
import com.wallet.service.wallet.dto.TransferRequest;
import com.wallet.service.wallet.dto.WalletBalanceResponse;
import com.wallet.service.wallet.dto.WithdrawRequest;
import com.wallet.service.wallet.service.WalletService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallet Management", description = "APIs for managing digital wallets, balances, and transactions")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {
    
    private final WalletService walletService;
    
    @GetMapping("/balance")
    @Timed(value = "wallet.balance", description = "Time taken to get wallet balance")
    @Operation(summary = "Get wallet balance", description = "Retrieve the current balance and status of the user's wallet")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getBalance(
            @Parameter(hidden = true) Authentication authentication) {
        log.info("GET /wallet/balance - User: {}", authentication.getName());
        
        WalletBalanceResponse balance = walletService.getBalance(authentication.getName());
        
        return ResponseEntity.ok(
            ApiResponse.success(balance, "Balance retrieved successfully")
        );
    }
    
    @PostMapping("/deposit")
    @Timed(value = "wallet.deposit", description = "Time taken to process deposit")
    @Operation(summary = "Deposit funds", description = "Deposit money into the wallet via payment gateway (Stripe/PayPal)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Deposit processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
        @Valid @RequestBody DepositRequest request,
        @Parameter(hidden = true) Authentication authentication
    ) {
        log.info("POST /wallet/deposit - User: {}, Amount: {}", 
            authentication.getName(), request.getAmount());
        
        TransactionResponse transaction = walletService.deposit(authentication.getName(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(transaction, "Deposit processed successfully"));
    }
    
    @PostMapping("/withdraw")
    @Timed(value = "wallet.withdraw", description = "Time taken to process withdrawal")
    @Operation(summary = "Withdraw funds", description = "Withdraw money from the wallet to bank account")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Withdrawal processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient balance or invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
        @Valid @RequestBody WithdrawRequest request,
        @Parameter(hidden = true) Authentication authentication
    ) {
        log.info("POST /wallet/withdraw - User: {}, Amount: {}", 
            authentication.getName(), request.getAmount());
        
        TransactionResponse transaction = walletService.withdraw(authentication.getName(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(transaction, "Withdrawal processed successfully"));
    }
    
    @PostMapping("/pay")
    @Timed(value = "wallet.pay", description = "Time taken to process payment")
    @Operation(summary = "Make payment", description = "Make a payment using wallet balance to a merchant")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient balance or invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> pay(
        @Valid @RequestBody PaymentRequest request,
        @Parameter(hidden = true) Authentication authentication
    ) {
        log.info("POST /wallet/pay - User: {}, Amount: {}, Merchant: {}", 
            authentication.getName(), request.getAmount(), request.getMerchantId());
        
        TransactionResponse transaction = walletService.pay(authentication.getName(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(transaction, "Payment processed successfully"));
    }
    
    @PostMapping("/transfer")
    @Timed(value = "wallet.transfer", description = "Time taken to process transfer")
    @Operation(summary = "Transfer funds", description = "Transfer money from your wallet to another user's wallet")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Transfer processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient balance or invalid target wallet"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
        @Valid @RequestBody TransferRequest request,
        @Parameter(hidden = true) Authentication authentication
    ) {
        log.info("POST /wallet/transfer - User: {}, Target: {}, Amount: {}", 
            authentication.getName(), request.getTargetWalletId(), request.getAmount());
        
        TransactionResponse transaction = walletService.transfer(authentication.getName(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(transaction, "Transfer processed successfully"));
    }
    
    @GetMapping("/transactions")
    @Timed(value = "wallet.transactions", description = "Time taken to get transactions")
    @Operation(summary = "Get transaction history", description = "Retrieve all transactions for the user's wallet")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
        @Parameter(hidden = true) Authentication authentication
    ) {
        log.info("GET /wallet/transactions - User: {}", authentication.getName());
        
        List<TransactionResponse> transactions = walletService.getTransactions(authentication.getName());
        
        return ResponseEntity.ok(
            ApiResponse.success(transactions, "Transactions retrieved successfully")
        );
    }
    
    @GetMapping("/transactions/paginated")
    @Timed(value = "wallet.transactions.paginated", description = "Time taken to get paginated transactions")
    @Operation(summary = "Get paginated transactions", description = "Retrieve paginated transaction history with sorting")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionsPaginated(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @Parameter(hidden = true) Authentication authentication
    ) {
        log.info("GET /wallet/transactions/paginated - User: {}, Page: {}", 
            authentication.getName(), pageable.getPageNumber());
        
        Page<TransactionResponse> transactions = 
            walletService.getTransactionsPaginated(authentication.getName(), pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(transactions, "Transactions retrieved successfully")
        );
    }
}
