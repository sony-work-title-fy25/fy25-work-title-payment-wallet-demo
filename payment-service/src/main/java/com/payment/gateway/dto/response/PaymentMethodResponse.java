package com.payment.gateway.dto.response;

import com.payment.gateway.entity.PaymentMethodType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponse {

    private UUID id;
    private PaymentMethodType type;
    private String nickname;
    private String displayName;
    private String lastFourDigits;
    private String cardBrand;
    private String expiryMonth;
    private String expiryYear;
    private String billingName;
    private String billingCountry;
    private boolean isDefault;
    private boolean isActive;
    private boolean isVerified;
    private boolean isExpired;
    private LocalDateTime createdAt;
}
