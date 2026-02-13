package com.summitbra.videoframeextractor.infrastructure.adapter.out.filesystem;

import com.summitbra.videoframeextractor.application.port.out.FileSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
public class FileSystemAdapter implements FileSystemService {

    @Value("${app.video.temp-dir:temp}")
    private String tempDirectory;

    @Value("${app.video.output-dir:output}")
    private String outputDirectory;

    @Override
    public Path createTempDirectory(String prefix) {
        try {
            Path tempDir = Paths.get(tempDirectory);
            Files.createDirectories(tempDir);
            return Files.createTempDirectory(tempDir, prefix);
        } catch (IOException e) {
            throw new FileSystemException("Erro ao criar diretório temporário", e);
        }
    }

    @Override
    public Path saveVideoFile(byte[] content, String filename, String directory) {
        try {
            Path dirPath = Paths.get(directory);
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(filename);
            Files.write(filePath, content);

            log.debug("Arquivo salvo: {}", filePath);
            return filePath;
        } catch (IOException e) {
            throw new FileSystemException("Erro ao salvar arquivo de vídeo", e);
        }
    }

    @Override
    public Path createZipFile(Path framesDirectory, String videoId, String outputDirectory) {
        try {
            Files.createDirectories(Paths.get(outputDirectory));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String zipFileName = String.format("frames_%s_%s.zip", videoId, timestamp);
            Path zipPath = Paths.get(outputDirectory, zipFileName);

            try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                Files.walk(framesDirectory)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String entryName = framesDirectory.relativize(file).toString();
                            ZipEntry entry = new ZipEntry(entryName);
                            zos.putNextEntry(entry);
                            Files.copy(file, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            log.warn("Erro ao adicionar arquivo {} ao ZIP", file, e);
                        }
                    });
            }

            log.info("Arquivo ZIP criado: {}", zipPath);
            return zipPath;
        } catch (IOException e) {
            throw new FileSystemException("Erro ao criar arquivo ZIP", e);
        }
    }

    @Override
    public void deleteFile(Path filePath) {
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("Arquivo removido: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Erro ao remover arquivo: {}", filePath, e);
        }
    }

    @Override
    public void deleteDirectory(Path directoryPath) {
        try {
            if (Files.exists(directoryPath)) {
                Files.walk(directoryPath)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("Erro ao remover: {}", path, e);
                        }
                    });
                log.debug("Diretório removido: {}", directoryPath);
            }
        } catch (IOException e) {
            log.warn("Erro ao remover diretório: {}", directoryPath, e);
        }
    }

    @Override
    public boolean fileExists(Path filePath) {
        return Files.exists(filePath);
    }

    @Override
    public Path getFilePath(String directory, String filename) {
        return Paths.get(directory, filename);
    }

    public static class FileSystemException extends RuntimeException {
        public FileSystemException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
