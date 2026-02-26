package com.fiap.videoframeextractor.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
@Slf4j
public class FFmpegConfig {

    @Value("${ffmpeg.path:/usr/bin/ffmpeg}")
    private String ffmpegPath;

    @Value("${frame.extraction.output-dir:./frames}")
    private String outputDirectory;

    @PostConstruct
    public void init() {
        // Verify FFmpeg installation
        File ffmpegFile = new File(ffmpegPath);
        if (!ffmpegFile.exists()) {
            log.warn("FFmpeg not found at: {}. Frame extraction may fail if FFmpeg is not in PATH.", ffmpegPath);
        } else {
            log.info("FFmpeg found at: {}", ffmpegPath);
        }

        // Create output directory if it doesn't exist
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (created) {
                log.info("Created output directory: {}", outputDirectory);
            } else {
                log.warn("Failed to create output directory: {}", outputDirectory);
            }
        } else {
            log.info("Output directory exists: {}", outputDirectory);
        }
    }
}
