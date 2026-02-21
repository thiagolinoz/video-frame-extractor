FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /app
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN mvn dependency:go-offline -B
COPY src src
RUN mvn clean package -DskipTests -B

FROM amazoncorretto:21-alpine-jdk
RUN apk add --no-cache ffmpeg wget && \
    addgroup -g 1001 app && \
    adduser -D -u 1001 -G app app
USER app
WORKDIR /app
COPY --from=builder --chown=app:app /app/target/videoFrameExtractor-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]