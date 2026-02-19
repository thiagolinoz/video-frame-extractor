package com.fiap.videoframeextractor.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Slf4j
public class DirectoryInitializer implements CommandLineRunner {

    @Value("${app.video.temp-dir:temp}")
    private String tempDirectory;

    @Value("${app.video.output-dir:output}")
    private String outputDirectory;

    @Override
    public void run(String... args) throws Exception {
        createDirectories();
        validateFFmpegAvailability();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(tempDirectory));
            Files.createDirectories(Paths.get(outputDirectory));
            log.info("Diretórios criados: temp={}, output={}", tempDirectory, outputDirectory);
        } catch (IOException e) {
            log.error("Erro ao criar diretórios", e);
            throw new RuntimeException("Falha na inicialização dos diretórios", e);
        }
    }

    private void validateFFmpegAvailability() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("FFmpeg disponível no sistema");
            } else {
                log.warn("FFmpeg pode não estar disponível (código de saída: {})", exitCode);
            }
        } catch (Exception e) {
            log.warn("FFmpeg não encontrado no PATH. Certifique-se de que o FFmpeg está instalado: {}",
                e.getMessage());
        }
    }
}
