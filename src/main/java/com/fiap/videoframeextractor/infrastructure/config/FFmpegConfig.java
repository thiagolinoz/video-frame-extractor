package com.fiap.videoframeextractor.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@Configuration
@Slf4j
public class FFmpegConfig {

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${app.ffprobe.path:ffprobe}")
    private String ffprobePath;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.ffmpeg.fallback-enabled", havingValue = "true", matchIfMissing = true)
    public FFmpeg ffmpeg() {
        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
            log.info("FFmpeg inicializado com sucesso: {}", ffmpegPath);
            return ffmpeg;
        } catch (IOException e) {
            log.warn("FFmpeg não encontrado no PATH ({}). Modo demonstração ativado. " +
                    "Para usar processamento real, instale o FFmpeg: https://ffmpeg.org/download.html",
                    ffmpegPath);
            return null;
        }
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.ffmpeg.fallback-enabled", havingValue = "true", matchIfMissing = true)
    public FFprobe ffprobe() {
        try {
            FFprobe ffprobe = new FFprobe(ffprobePath);
            log.info("FFprobe inicializado com sucesso: {}", ffprobePath);
            return ffprobe;
        } catch (IOException e) {
            log.warn("FFprobe não encontrado no PATH ({}). Modo demonstração ativado.", ffprobePath);
            return null; // Retorna null - será tratado nos adapters
        }
    }

    private FFmpeg createMockFFmpeg() {
        return null;
    }

    private FFprobe createMockFFprobe() {
        return null;
    }
}
