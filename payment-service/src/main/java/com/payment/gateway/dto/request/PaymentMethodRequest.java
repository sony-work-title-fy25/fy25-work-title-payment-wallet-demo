package com.payment.gateway.dto.request;

import com.payment.gateway.entity.PaymentMethodType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodRequest {

    @NotNull(message = "Payment method type is required")
    private PaymentMethodType type;

    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    private String nickname;

    @NotBlank(message = "Provider token is required")
    @Size(max = 200, message = "Provider token cannot exceed 200 characters")
    private String providerToken;

    @Size(min = 4, max = 4, message = "Last four digits must be exactly 4 characters")
    private String lastFourDigits;

    @Size(max = 50, message = "Card brand cannot exceed 50 characters")
    private String cardBrand;

    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Invalid expiry month format")
    private String expiryMonth;

    @Pattern(regexp = "^\\d{4}$", message = "Invalid expiry year format")
    private String expiryYear;

    @Size(max = 100, message = "Billing name cannot exceed 100 characters")
    private String billingName;

    @Size(max = 200, message = "Billing address cannot exceed 200 characters")
    private String billingAddress;

    @Size(max = 100, message = "Billing city cannot exceed 100 characters")
    private String billingCity;

    @Size(max = 50, message = "Billing state cannot exceed 50 characters")
    private String billingState;

    @Size(max = 20, message = "Billing postal code cannot exceed 20 characters")
    private String billingPostalCode;

    @Size(min = 2, max = 3, message = "Billing country must be a valid ISO code")
    private String billingCountry;

    private boolean setAsDefault;
}
