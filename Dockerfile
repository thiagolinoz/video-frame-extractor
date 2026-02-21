# Multi-stage build for optimization
FROM maven:3.9.6-amazoncorretto-21 AS builder

WORKDIR /app

# Copy Maven files first for better layer caching
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM maven:3.9.6-amazoncorretto-21 AS runtime

# Install FFmpeg and create user in single layer
RUN apk add --no-cache ffmpeg shadow && \
    addgroup -g 1001 video-frame-extractor && \
    adduser -D -u 1001 -G video-frame-extractor video-frame-extractor && \
    apk del shadow

# Create directories with proper permissions
RUN mkdir -p /app/temp /app/output /app/logs && \
    chown -R video-frame-extractor:video-frame-extractor /app

# Switch to non-root user
USER video-frame-extractor

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder --chown=video-frame-extractor:video-frame-extractor /app/target/videoFrameExtractor-0.0.1-SNAPSHOT.jar app.jar

# Expose the correct port from application.yml
EXPOSE 8085

# Add health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Optimize JVM settings for containers
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
