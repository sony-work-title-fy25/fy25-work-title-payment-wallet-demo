# Wallet Service

A comprehensive digital wallet management service integrated into the Unified Payment Gateway ecosystem.

## Overview

The Wallet Service provides secure digital wallet operations including deposits, withdrawals, payments, and wallet-to-wallet transfers. It integrates seamlessly with the Payment Gateway and API Gateway services through Eureka service discovery.

## Features

- ✅ **Wallet Management**: Create and manage digital wallets
- ✅ **Deposits**: Add funds via Stripe/PayPal payment gateways
- ✅ **Withdrawals**: Transfer funds to bank accounts
- ✅ **Payments**: Pay merchants using wallet balance
- ✅ **Transfers**: Send money to other wallets
- ✅ **Transaction History**: View all transactions with pagination
- ✅ **Security**: JWT-based authentication via API Gateway
- ✅ **Event-Driven**: Kafka-based event processing
- ✅ **Caching**: Redis caching for improved performance
- ✅ **Concurrent Safety**: Pessimistic locking for wallet operations
- ✅ **Observability**: Prometheus metrics and health checks

## Tech Stack

- **Framework**: Spring Boot 3.5.x
- **Java Version**: Java 21
- **Database**: PostgreSQL (Production), H2 (Development)
- **Cache**: Redis with Lettuce
- **Message Queue**: Apache Kafka
- **Security**: Spring Security + OAuth2 JWT Resource Server
- **Payment Gateways**: Stripe SDK, PayPal SDK
- **Service Discovery**: Netflix Eureka Client
- **API Documentation**: SpringDoc OpenAPI 3.0 (Swagger)
- **Observability**: Micrometer + Prometheus

## Quick Start

### Development Mode (Standalone)

```bash
# Run with H2 in-memory database (no external dependencies)
cd wallet-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Access:**
- API: http://localhost:8083/api/v1/wallet
- Swagger UI: http://localhost:8083/swagger-ui.html
- H2 Console: http://localhost:8083/h2-console
  - JDBC URL: `jdbc:h2:mem:walletdb`
  - Username: `sa`
  - Password: (empty)

### Production Mode (via API Gateway)

```bash
# 1. Start Eureka Server
cd ../eureka-server
mvn spring-boot:run

# 2. Start Wallet Service
cd ../wallet-service
mvn spring-boot:run

# 3. Start API Gateway
cd ../api-gateway
mvn spring-boot:run
```

**Access via API Gateway:**
- http://localhost:8085/api/v1/wallet/*

## API Endpoints

All endpoints require JWT authentication via the API Gateway.

### Wallet Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/wallet/balance` | Get wallet balance |
| `POST` | `/api/v1/wallet/deposit` | Deposit funds via payment gateway |
| `POST` | `/api/v1/wallet/withdraw` | Withdraw funds to bank account |
| `POST` | `/api/v1/wallet/pay` | Make payment using wallet balance |
| `POST` | `/api/v1/wallet/transfer` | Transfer funds to another wallet |
| `GET` | `/api/v1/wallet/transactions` | Get all transactions |
| `GET` | `/api/v1/wallet/transactions/paginated` | Get paginated transactions |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/health` | Service health check (no auth) |

## Configuration

### Application Properties

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8083

spring:
  application:
    name: wallet-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/walletdb
    username: wallet_user
    password: wallet_pass

payment:
  stripe:
    api-key: ${STRIPE_API_KEY}
  paypal:
    client-id: ${PAYPAL_CLIENT_ID}
    client-secret: ${PAYPAL_CLIENT_SECRET}

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `wallet_user` | PostgreSQL username |
| `DB_PASSWORD` | `wallet_pass` | PostgreSQL password |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `STRIPE_API_KEY` | - | Stripe API secret key |
| `PAYPAL_CLIENT_ID` | - | PayPal client ID |
| `PAYPAL_CLIENT_SECRET` | - | PayPal client secret |
| `EUREKA_URI` | `http://localhost:8761/eureka` | Eureka server URL |

## Database Schema

### Main Entities

- **User**: User account information
- **Wallet**: Digital wallet with balance and status
- **Transaction**: Transaction records (deposits, withdrawals, payments, transfers)

### Transaction Types

- `DEPOSIT` - Add funds to wallet
- `WITHDRAWAL` - Remove funds from wallet
- `PAYMENT` - Pay merchant using wallet balance
- `TRANSFER_OUT` - Send money to another wallet
- `TRANSFER_IN` - Receive money from another wallet
- `REFUND` - Refund transaction
- `ADJUSTMENT` - Manual balance adjustment

## Event-Driven Architecture

### Kafka Topics

1. **transaction-events**: All transaction events
2. **wallet-events**: Wallet balance updates and state changes
3. **payment-events**: Payment gateway events

### Event Listeners

- `TransactionEventListener`: Processes transaction events
- `WalletEventListener`: Processes wallet state changes

## Monitoring & Metrics

### Custom Metrics

- `wallet.deposit.success` - Successful deposits
- `wallet.withdraw.success` - Successful withdrawals
- `wallet.payment.success` - Successful payments
- `wallet.transfer.success` - Successful transfers
- `wallet.withdraw.insufficient_balance` - Balance failures

### Prometheus Endpoint

```
http://localhost:8083/actuator/prometheus
```

## Security Features

1. **JWT Authentication** - All endpoints require Bearer token
2. **Pessimistic Locking** - Prevents race conditions in concurrent transactions
3. **Version Control** - Optimistic locking on wallet entity
4. **ACID Compliance** - Database transaction isolation

## Integration with Payment Gateway

The Wallet Service integrates with the existing Payment Gateway ecosystem:

- **Service Discovery**: Registers with Eureka for load-balanced routing
- **API Gateway**: All requests routed through API Gateway at port 8085
- **Shared Infrastructure**: Uses same PostgreSQL, Redis, and Kafka instances
- **Event Coordination**: Publishes events to shared Kafka topics

## Docker Deployment

The wallet service is included in the main `docker-compose.yml`:

```bash
# Start all services
docker-compose up -d

# Start only wallet service dependencies
docker-compose up -d eureka-server postgres redis kafka wallet-service
```

## Development Notes

- **Package Structure**: `com.wallet.service.wallet.*`
- **Port**: 8083
- **Service Name**: `wallet-service`
- **Eureka Instance ID**: `wallet-service:8083`

## Future Enhancements

- Multi-currency support
- Scheduled payments
- KYC integration
- Transaction limits and controls
- Webhooks for external notifications
- Advanced fraud detection

## Support

For issues or questions, please refer to the main project README or contact the development team.
