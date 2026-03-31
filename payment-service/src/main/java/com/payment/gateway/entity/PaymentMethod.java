package com.payment.gateway.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_payment_method_user_id", columnList = "userId"),
        @Index(name = "idx_payment_method_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethodType type;

    @Column(length = 100)
    private String nickname;

    @Column(nullable = false, length = 200)
    private String providerToken;

    @Column(length = 4)
    private String lastFourDigits;

    @Column(length = 50)
    private String cardBrand;

    @Column(length = 2)
    private String expiryMonth;

    @Column(length = 4)
    private String expiryYear;

    @Column(length = 100)
    private String billingName;

    @Column(length = 200)
    private String billingAddress;

    @Column(length = 100)
    private String billingCity;

    @Column(length = 50)
    private String billingState;

    @Column(length = 20)
    private String billingPostalCode;

    @Column(length = 3)
    private String billingCountry;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int expYear = Integer.parseInt(expiryYear);
        int expMonth = Integer.parseInt(expiryMonth);

        return expYear < currentYear || (expYear == currentYear && expMonth < currentMonth);
    }

    public String getDisplayName() {
        if (type == PaymentMethodType.CREDIT_CARD || type == PaymentMethodType.DEBIT_CARD) {
            return String.format("%s •••• %s", cardBrand != null ? cardBrand : "Card", lastFourDigits);
        }
        return nickname != null ? nickname : type.name();
    }
}
