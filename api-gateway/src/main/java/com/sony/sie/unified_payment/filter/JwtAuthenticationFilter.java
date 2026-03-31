package com.sony.sie.unified_payment.filter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String secretKey;

     // Skip JWT authentication for public endpoints that don't require auth.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || path.startsWith("/actuator/")
                || path.startsWith("/api/v1/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            sendError(response, "Authorization header is missing", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = null;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            sendError(response, "Authorization header must be Bearer token", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!isValidJwt(token)) {
            sendError(response, "Invalid or expired JWT token", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            String userEmail = claims.getSubject();
            String userRole = claims.getStringClaim("role");
            Long userId = claims.getLongClaim("userId");

            if (userEmail == null || userRole == null || userId == null) {
                sendError(response, "Missing required claims in JWT token", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Set user context as request attributes for downstream proxy forwarding
            request.setAttribute("X-User-Email", userEmail);
            request.setAttribute("X-User-Role", userRole);
            request.setAttribute("X-User-Id", userId.toString());

            // Set Spring Security authentication context
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + userRole))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated user: {} with role: {}", userEmail, userRole);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
            sendError(response, "Error processing JWT token: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidJwt(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getExpirationTime().after(new Date());
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
