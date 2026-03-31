package com.payment.gateway.controller;

import com.payment.gateway.dto.request.PaymentInitiateRequest;
import com.payment.gateway.dto.request.RefundRequest;
import com.payment.gateway.dto.response.ApiResponse;
import com.payment.gateway.dto.response.PagedResponse;
import com.payment.gateway.dto.response.RefundResponse;
import com.payment.gateway.dto.response.TransactionResponse;
import com.payment.gateway.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment transaction management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate a new payment", description = "Creates a new payment transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("POST /api/v1/payments/initiate - User: {}", userId);
        TransactionResponse response = paymentService.initiatePayment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Payment initiated successfully"));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a payment", description = "Processes and confirms a pending payment")
    public ResponseEntity<ApiResponse<TransactionResponse>> confirmPayment(
            @Parameter(description = "Transaction ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("POST /api/v1/payments/{}/confirm - User: {}", id, userId);
        TransactionResponse response = paymentService.confirmPayment(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment confirmed successfully"));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a payment", description = "Cancels a pending payment transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> cancelPayment(
            @Parameter(description = "Transaction ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("POST /api/v1/payments/{}/cancel - User: {}", id, userId);
        TransactionResponse response = paymentService.cancelPayment(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment cancelled successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction details", description = "Retrieves details of a specific transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @Parameter(description = "Transaction ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("GET /api/v1/payments/{} - User: {}", id, userId);
        TransactionResponse response = paymentService.getTransaction(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get transaction history", description = "Retrieves paginated transaction history")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getTransactionHistory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("GET /api/v1/payments/history - User: {} Page: {} Size: {}", userId, page, size);
        PagedResponse<TransactionResponse> response = paymentService.getTransactionHistory(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Request a refund", description = "Initiates a refund for a completed transaction")
    public ResponseEntity<ApiResponse<RefundResponse>> initiateRefund(
            @Parameter(description = "Transaction ID") @PathVariable UUID id,
            @Valid @RequestBody RefundRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("POST /api/v1/payments/{}/refund - User: {}", id, userId);
        RefundResponse response = paymentService.initiateRefund(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Refund initiated successfully"));
    }

    private String extractUserId(Jwt jwt) {
        if (jwt == null) return "anonymous";
        return jwt.getClaimAsString("sub");
    }
}
