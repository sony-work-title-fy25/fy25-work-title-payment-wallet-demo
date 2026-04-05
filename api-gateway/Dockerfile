# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM and module POMs first for dependency caching
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY wallet-service/pom.xml wallet-service/pom.xml

# Download dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -pl api-gateway -am -B || true

# Copy source code
COPY api-gateway/src api-gateway/src
COPY logback-spring.xml logback-spring.xml

# Build only the api-gateway module
RUN mvn clean package -pl api-gateway -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN addgroup --system gateway && adduser --system --ingroup gateway gateway
USER gateway

# Copy the built JAR
COPY --from=build /app/api-gateway/target/*.jar app.jar

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
