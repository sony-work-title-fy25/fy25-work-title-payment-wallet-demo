# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM and module POMs first for dependency caching
COPY pom.xml .
COPY payment-service/pom.xml payment-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY wallet-service/pom.xml wallet-service/pom.xml

# Download dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -pl payment-service -am -B || true

# Copy source code
COPY payment-service/src payment-service/src
COPY logback-spring.xml logback-spring.xml

# Build only the payment-service module
RUN mvn clean package -pl payment-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN addgroup --system payment && adduser --system --ingroup payment payment
USER payment

# Copy the built JAR
COPY --from=build /app/payment-service/target/*.jar app.jar

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/api/v1/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
