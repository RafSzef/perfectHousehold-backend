# ==============================================================================
# Multi-stage Dockerfile for PerfectHousehold Backend (Java 21 / Spring Boot 3)
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Build Stage
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and package jar
COPY src ./src
RUN mvn clean package -DskipTests

# Extract Spring Boot layered jar for lightning-fast container startup and caching
WORKDIR /build/target/extracted
RUN java -Djarmode=layertools -jar /build/target/*.jar extract

# ------------------------------------------------------------------------------
# 2. Production Runtime Stage (Lightweight JRE)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Run as dedicated unprivileged user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy Spring Boot layers in optimal order (least changed -> most changed)
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/application/ ./

USER appuser:appgroup

# Production JVM tuning for Oracle VM / Cloud containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
