package com.payment.gateway.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Provides an HS256 {@link JwtDecoder} so the payment-service can run with
 * {@code oauth2ResourceServer().jwt()} enabled (non-dev profiles).
 *
 * The API Gateway in this repo also uses an HMAC shared secret for JWT validation.
 */
@Configuration
@Profile("!dev")
public class JwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        // HS256 requires a sufficiently long secret (>= 256 bits recommended).
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}

