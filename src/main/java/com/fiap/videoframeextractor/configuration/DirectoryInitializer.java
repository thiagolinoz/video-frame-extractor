package com.fiap.videoframeextractor.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class DirectoryInitializer implements CommandLineRunner {

    @Value("${frame.extraction.output-dir:./frames}")
    private String outputDirectory;

    @Override
    public void run(String... args) {
        File directory = new File(outputDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Created output directory: {}", outputDirectory);
            } else {
                log.warn("Failed to create output directory: {}", outputDirectory);
            }
        } else {
            log.info("Output directory already exists: {}", outputDirectory);
        }
    }
}
