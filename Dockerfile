# ─── Stage 1: Build stage ───────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Install dos2unix in case mvnw has CRLF line endings from Windows
RUN apk add --no-cache dos2unix

# Copy maven wrapper and pom.xml first for dependency layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN dos2unix mvnw && chmod +x mvnw

# Download dependencies offline (cached layer unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ─── Stage 2: Production runtime stage ─────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Install curl for Actuator health probes
RUN apk add --no-cache curl

# Create a non-root group and user (UID/GID 10001)
RUN addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup

WORKDIR /app

# Copy built JAR from builder stage and assign ownership
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar /app/app.jar

# Run as non-root user
USER appuser:appgroup

# Container-aware JVM memory settings and secure random source
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Health check using Spring Boot Actuator probe
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
