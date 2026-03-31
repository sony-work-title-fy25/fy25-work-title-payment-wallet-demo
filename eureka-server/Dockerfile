# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent POM and module POMs first for dependency caching
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY wallet-service/pom.xml wallet-service/pom.xml

# Download dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -pl eureka-server -am -B || true

# Copy source code
COPY eureka-server/src eureka-server/src

# Build only the eureka-server module
RUN mvn clean package -pl eureka-server -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user
RUN addgroup --system eureka && adduser --system --ingroup eureka eureka
USER eureka

# Copy the built JAR
COPY --from=build /app/eureka-server/target/*.jar app.jar

# Expose port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
