FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache ffmpeg

COPY target/videoFrameExtractor-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]