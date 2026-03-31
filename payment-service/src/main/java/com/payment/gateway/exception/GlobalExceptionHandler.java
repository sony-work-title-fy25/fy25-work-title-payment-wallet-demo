package com.payment.gateway.exception;

import com.payment.gateway.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentException(PaymentException ex) {
        log.error("Payment error: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .error(ApiResponse.ErrorDetails.builder()
                        .code(ex.getErrorCode())
                        .message(ex.getMessage())
                        .details(ex.getDetails())
                        .build())
                .build();

        HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation error: {}", errors);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("Validation failed")
                .error(ApiResponse.ErrorDetails.builder()
                        .code("VALIDATION_ERROR")
                        .message("Invalid request data")
                        .details(errors)
                        .build())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ApiResponse<Void> response = ApiResponse.error("An unexpected error occurred. Please try again later.", "INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private HttpStatus mapErrorCodeToStatus(String errorCode) {
        return switch (errorCode) {
            case PaymentException.TRANSACTION_NOT_FOUND, PaymentException.PAYMENT_METHOD_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case PaymentException.UNAUTHORIZED_ACCESS -> HttpStatus.FORBIDDEN;
            case PaymentException.INVALID_PAYMENT_STATE, PaymentException.REFUND_NOT_ALLOWED,
                 PaymentException.REFUND_AMOUNT_EXCEEDED, PaymentException.PAYMENT_METHOD_EXPIRED,
                 PaymentException.DUPLICATE_PAYMENT_METHOD -> HttpStatus.BAD_REQUEST;
            case PaymentException.INSUFFICIENT_FUNDS -> HttpStatus.PAYMENT_REQUIRED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
