package com.payment.gateway.dto.request;

import com.payment.gateway.entity.RefundReason;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Refund reason is required")
    private RefundReason reason;

    @Size(max = 500, message = "Reason details cannot exceed 500 characters")
    private String reasonDetails;
}
