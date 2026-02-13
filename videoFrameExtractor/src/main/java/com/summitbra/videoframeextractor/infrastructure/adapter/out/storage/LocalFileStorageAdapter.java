package com.summitbra.videoframeextractor.infrastructure.adapter.out.storage;

import com.summitbra.videoframeextractor.application.port.out.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class LocalFileStorageAdapter implements StorageService {

    @Value("${app.storage.base-path:storage}")
    private String basePath;

    @Override
    public byte[] downloadFile(String bucketName, String key) {
        try {
            Path filePath = Paths.get(basePath, bucketName, key);
            if (!Files.exists(filePath)) {
                log.warn("Arquivo não encontrado: {}", filePath);
                return new byte[0];
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Erro ao baixar arquivo {}/{}: {}", bucketName, key, e.getMessage(), e);
            throw new StorageException("Erro ao baixar arquivo", e);
        }
    }

    @Override
    public void uploadFile(String bucketName, String key, byte[] content) {
        try {
            Path filePath = Paths.get(basePath, bucketName, key);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content);
            log.info("Arquivo enviado para: {}", filePath);
        } catch (IOException e) {
            log.error("Erro ao enviar arquivo {}/{}: {}", bucketName, key, e.getMessage(), e);
            throw new StorageException("Erro ao enviar arquivo", e);
        }
    }

    @Override
    public void uploadFile(String bucketName, String key, Path filePath) {
        try {
            byte[] content = Files.readAllBytes(filePath);
            uploadFile(bucketName, key, content);
        } catch (IOException e) {
            log.error("Erro ao ler arquivo para upload: {}", filePath, e);
            throw new StorageException("Erro ao ler arquivo para upload", e);
        }
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
