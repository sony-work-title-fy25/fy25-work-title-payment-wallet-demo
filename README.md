# FY25 Work Title Payment Wallet Demo

This repository demonstrates a microservices-based payment and wallet system using Spring Boot, Docker, and Eureka for service discovery.

## Project Structure

- **api-gateway/**: API Gateway for routing requests to backend services.
- **eureka-server/**: Eureka Service Registry for service discovery.
- **payment-service/**: Handles payment processing logic.
- **wallet-service/**: Manages wallet operations.

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose

## Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd fy25-work-title-payment-wallet-demo
```

### 2. Build the Services
```bash
mvn clean package -DskipTests
```

### 3. Run with Docker Compose
```bash
docker-compose up --build
```

### 4. Access the Services
- Eureka Dashboard: [http://localhost:8761](http://localhost:8761)
- API Gateway: [http://localhost:8080](http://localhost:8080)

## Configuration
- Each service contains its own `application.yml` (and profiles) under `src/main/resources`.
- Database schema for payment-service is in `payment-service/src/main/resources/schema.sql`.

## Useful Commands
- Build a single service:
  ```bash
  cd <service-folder>
  mvn clean package -DskipTests
  ```
- Run a service locally:
  ```bash
  java -jar target/<service-jar>.jar
  ```

## License
This project is for demonstration purposes only.

