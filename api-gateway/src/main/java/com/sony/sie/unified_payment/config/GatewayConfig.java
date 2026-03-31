package com.sony.sie.unified_payment.config;

import java.net.URI;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Gateway route configuration that forwards requests to the Payment-Gateway and Wallet services.
 * Uses Spring Cloud Gateway MVC with Eureka service discovery for load-balanced routing.
 *
 * Routes:
 *   /api/v1/payments/**         → Payment transactions
 *   /api/v1/payment-methods/**  → Payment method management
 *   /api/v1/wallet/**           → Wallet operations
 *   /api/v1/health/**           → Service health checks
 */
@Configuration
public class GatewayConfig {

    @Value("${services.payment-gateway.url}")
    private String paymentServiceUrl;

    @Value("${services.wallet-service.url:lb://wallet-service}")
    private String walletServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> paymentServiceRoutes() {
        URI serviceUri = URI.create(paymentServiceUrl);

        return GatewayRouterFunctions.route("payment-transactions")
                .route(RequestPredicates.path("/api/v1/payments/**"), HandlerFunctions.http(serviceUri))
                .before(userContextHeaderRelay())
                .build()
            .and(
                GatewayRouterFunctions.route("payment-methods")
                    .route(RequestPredicates.path("/api/v1/payment-methods/**"), HandlerFunctions.http(serviceUri))
                    .before(userContextHeaderRelay())
                    .build()
            ).and(
                GatewayRouterFunctions.route("payment-health")
                    .route(RequestPredicates.path("/api/v1/health/**"), HandlerFunctions.http(serviceUri))
                    .build()
            );
    }

    @Bean
    public RouterFunction<ServerResponse> walletServiceRoutes() {
        URI serviceUri = URI.create(walletServiceUrl);

        return GatewayRouterFunctions.route("wallet-operations")
                .route(RequestPredicates.path("/api/v1/wallet/**"), HandlerFunctions.http(serviceUri))
                .before(userContextHeaderRelay())
                .build();
    }

    /**
     * Creates a before-filter that relays user context headers (set by the
     * JwtAuthenticationFilter as request attributes) to the downstream service.
     */
    private Function<ServerRequest, ServerRequest> userContextHeaderRelay() {
        return request -> {
            HttpServletRequest servletRequest = request.servletRequest();

            Object email = servletRequest.getAttribute("X-User-Email");
            Object role = servletRequest.getAttribute("X-User-Role");
            Object userId = servletRequest.getAttribute("X-User-Id");

            if (email == null && role == null && userId == null) {
                return request;
            }

            ServerRequest.Builder builder = ServerRequest.from(request);
            if (email != null) builder.header("X-User-Email", email.toString());
            if (role != null) builder.header("X-User-Role", role.toString());
            if (userId != null) builder.header("X-User-Id", userId.toString());
            builder.header("X-Forwarded-By", "api-gateway");

            return builder.build();
        };
    }
}
