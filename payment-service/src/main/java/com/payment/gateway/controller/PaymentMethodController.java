package com.payment.gateway.controller;

import com.payment.gateway.dto.request.PaymentMethodRequest;
import com.payment.gateway.dto.response.ApiResponse;
import com.payment.gateway.dto.response.PaymentMethodResponse;
import com.payment.gateway.service.PaymentMethodService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Methods", description = "Payment method management APIs")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    @Operation(summary = "Add payment method", description = "Adds a new payment method for the user")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> addPaymentMethod(
            @Valid @RequestBody PaymentMethodRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("POST /api/v1/payment-methods - User: {} Type: {}", userId, request.getType());
        PaymentMethodResponse response = paymentMethodService.addPaymentMethod(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Payment method added successfully"));
    }

    @GetMapping
    @Operation(summary = "Get payment methods", description = "Retrieves all payment methods for the user")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods(@AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("GET /api/v1/payment-methods - User: {}", userId);
        List<PaymentMethodResponse> response = paymentMethodService.getPaymentMethods(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment method", description = "Retrieves a specific payment method by ID")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getPaymentMethod(
            @Parameter(description = "Payment method ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("GET /api/v1/payment-methods/{} - User: {}", id, userId);
        PaymentMethodResponse response = paymentMethodService.getPaymentMethod(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment method", description = "Removes a payment method from the user's account")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(
            @Parameter(description = "Payment method ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("DELETE /api/v1/payment-methods/{} - User: {}", id, userId);
        paymentMethodService.deletePaymentMethod(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Payment method deleted successfully"));
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set default payment method", description = "Sets a payment method as the default")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> setDefaultPaymentMethod(
            @Parameter(description = "Payment method ID") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = extractUserId(jwt);
        log.info("PUT /api/v1/payment-methods/{}/default - User: {}", id, userId);
        PaymentMethodResponse response = paymentMethodService.setDefaultPaymentMethod(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Default payment method updated"));
    }

    private String extractUserId(Jwt jwt) {
        if (jwt == null) return "anonymous";
        return jwt.getClaimAsString("sub");
    }
}
