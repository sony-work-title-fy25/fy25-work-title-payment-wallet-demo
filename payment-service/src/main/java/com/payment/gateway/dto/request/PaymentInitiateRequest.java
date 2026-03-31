package com.payment.gateway.dto.request;

import com.payment.gateway.entity.PaymentType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiateRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Invalid amount format")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    private UUID paymentMethodId;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Merchant reference cannot exceed 100 characters")
    private String merchantReference;

    @Size(max = 100, message = "Product ID cannot exceed 100 characters")
    private String productId;

    @Size(max = 50, message = "Product type cannot exceed 50 characters")
    private String productType;

    @Size(max = 50, message = "Platform cannot exceed 50 characters")
    private String platform;

    @Size(max = 10, message = "Region cannot exceed 10 characters")
    private String region;

    private boolean useWalletBalance;

    private BigDecimal loyaltyPointsToRedeem;
}
