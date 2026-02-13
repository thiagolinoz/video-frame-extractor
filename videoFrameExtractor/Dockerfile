# Multi-stage build for optimized image
FROM maven:3.9.4-openjdk-21-slim AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime image
FROM openjdk:21-jdk-slim

# Install FFmpeg and curl for health checks
RUN apt-get update && \
    apt-get install -y ffmpeg curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Create directories
RUN mkdir -p /app/temp /app/output /app/logs && \
    chown -R appuser:appuser /app

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/videoFrameExtractor-*.jar app.jar

# Change ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Run with H2 profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
