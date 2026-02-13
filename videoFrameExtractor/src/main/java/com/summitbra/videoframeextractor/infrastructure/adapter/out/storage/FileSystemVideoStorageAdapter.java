package com.summitbra.videoframeextractor.infrastructure.adapter.out.storage;

import com.summitbra.videoframeextractor.application.port.out.VideoStoragePort;
import com.summitbra.videoframeextractor.domain.model.VideoFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
public class FileSystemVideoStorageAdapter implements VideoStoragePort {

    @Value("${app.video.temp-dir:temp}")
    private String tempDir;

    @Value("${app.video.output-dir:output}")
    private String outputDir;

    @Override
    public Path saveTemporaryFile(VideoFile videoFile) {
        try {
            Path tempPath = Paths.get(tempDir);
            Files.createDirectories(tempPath);

            String fileName = "video_" + System.currentTimeMillis() + "_" +
                            sanitizeFileName(videoFile.getFilename());
            Path filePath = tempPath.resolve(fileName);

            Files.write(filePath, videoFile.getContent());

            log.info("Arquivo temporário salvo: {}", filePath);
            return filePath;

        } catch (IOException e) {
            log.error("Erro ao salvar arquivo temporário", e);
            throw new VideoStorageException("Erro ao salvar arquivo temporário", e);
        }
    }

    @Override
    public boolean removeTemporaryFile(Path filePath) {
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Arquivo temporário removido: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Erro ao remover arquivo temporário: {}", filePath, e);
            return false;
        }
    }

    @Override
    public Path createFramesZip(Path framesDirectory, String zipFileName) {
        try {
            Path outputPath = Paths.get(outputDir);
            Files.createDirectories(outputPath);

            Path zipPath = outputPath.resolve(zipFileName);

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                Files.walkFileTree(framesDirectory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (Files.isRegularFile(file)) {
                            String relativePath = framesDirectory.relativize(file).toString();
                            ZipEntry entry = new ZipEntry(relativePath);
                            zos.putNextEntry(entry);
                            Files.copy(file, zos);
                            zos.closeEntry();
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            log.info("Arquivo ZIP criado: {}", zipPath);
            return zipPath;

        } catch (IOException e) {
            log.error("Erro ao criar arquivo ZIP", e);
            throw new VideoStorageException("Erro ao criar arquivo ZIP", e);
        }
    }

    @Override
    public boolean zipFileExists(String zipFileName) {
        Path zipPath = Paths.get(outputDir).resolve(zipFileName);
        return Files.exists(zipPath);
    }

    @Override
    public Path getZipFilePath(String zipFileName) {
        return Paths.get(outputDir).resolve(zipFileName);
    }

    @Override
    public int cleanupOldFiles(int olderThanHours) {
        AtomicInteger removedCount = new AtomicInteger(0);
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(olderThanHours);

        try {
            cleanupDirectory(Paths.get(tempDir), cutoffTime, removedCount);

            cleanupDirectory(Paths.get(outputDir), cutoffTime, removedCount);

        } catch (IOException e) {
            log.error("Erro durante limpeza de arquivos", e);
        }

        log.info("Limpeza concluída: {} arquivos removidos", removedCount.get());
        return removedCount.get();
    }

    private void cleanupDirectory(Path directory, LocalDateTime cutoffTime, AtomicInteger removedCount)
            throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                LocalDateTime fileTime = LocalDateTime.ofInstant(
                    attrs.lastModifiedTime().toInstant(),
                    ZoneId.systemDefault()
                );

                if (fileTime.isBefore(cutoffTime)) {
                    try {
                        Files.delete(file);
                        removedCount.incrementAndGet();
                        log.debug("Arquivo antigo removido: {}", file);
                    } catch (IOException e) {
                        log.warn("Falha ao remover arquivo: {}", file, e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public static class VideoStorageException extends RuntimeException {
        public VideoStorageException(String message) {
            super(message);
        }

        public VideoStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
