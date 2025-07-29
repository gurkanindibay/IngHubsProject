# Multi-stage build for Spring Boot application
FROM maven:3-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (for better layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Create app user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/wallet-service-*.jar app.jar

# Copy SSL keystore for HTTPS
COPY --from=build /app/src/main/resources/keystore.p12 keystore.p12

# Copy Docker-specific logback configuration
COPY --from=build /app/src/main/resources/logback-docker.xml logback-docker.xml

# Create directories with proper permissions (no logs directory needed for Docker)
RUN mkdir -p /app/data && \
    chown -R appuser:appgroup /app && \
    chmod -R 755 /app

# Switch to non-root user
USER appuser

# Expose ports (8080 for HTTP redirect, 8443 for HTTPS)
EXPOSE 8080 8443

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-check-certificate --quiet --spider https://localhost:8443/actuator/health || exit 1

# Run the application with Docker-specific logging configuration
ENTRYPOINT ["java", "-Dlogging.config=logback-docker.xml", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
