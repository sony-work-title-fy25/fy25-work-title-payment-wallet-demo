package com.payment.gateway.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.payment.gateway.entity.PaymentMethod;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    List<PaymentMethod> findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(String userId);

    Optional<PaymentMethod> findByIdAndUserId(UUID id, String userId);

    boolean existsByUserIdAndIsActiveTrue(String userId);

    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.userId = :userId")
    void clearDefaultForUser(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isActive = false WHERE pm.id = :id AND pm.userId = :userId")
    int softDeleteByIdAndUserId(@Param("id") UUID id, @Param("userId") String userId);

    Optional<PaymentMethod> findByUserIdAndProviderToken(String userId, String providerToken);
}
