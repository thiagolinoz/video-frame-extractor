# Imagem base com Java 21 e FFmpeg
FROM eclipse-temurin:21-jre-jammy

# Instalar FFmpeg
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR da aplicação
COPY target/videoFrameExtractor-*.jar app.jar

# Expor porta
EXPOSE 8080

# Executar aplicação
CMD ["java", "-jar", "app.jar"]