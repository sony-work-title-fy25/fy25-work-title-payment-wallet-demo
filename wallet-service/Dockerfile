# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM and module POMs first for dependency caching
COPY pom.xml .
COPY wallet-service/pom.xml wallet-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml

# Download dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -pl wallet-service -am -B || true

# Copy source code
COPY wallet-service/src wallet-service/src

# Build only the wallet-service module
RUN mvn clean package -pl wallet-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN addgroup --system wallet && adduser --system --ingroup wallet wallet
USER wallet

# Copy the built JAR
COPY --from=build /app/wallet-service/target/*.jar app.jar

# Expose port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
