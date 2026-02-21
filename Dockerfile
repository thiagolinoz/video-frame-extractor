FROM amazoncorretto:21-al2023-jdk

RUN dnf update -y && \
    dnf install -y ffmpeg && \
    dnf clean all

WORKDIR /app

COPY ./target/videoFrameExtractor-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]