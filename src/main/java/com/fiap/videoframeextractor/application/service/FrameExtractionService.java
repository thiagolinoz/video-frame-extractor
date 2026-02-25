package com.fiap.videoframeextractor.application.service;

import com.fiap.videoframeextractor.domain.model.VideoMessage;
import com.fiap.videoframeextractor.infrastructure.adapter.out.s3.S3StorageAdapter;
import com.fiap.videoframeextractor.infrastructure.adapter.out.ffmpeg.FFmpegFrameExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FrameExtractionService {

    //private final S3StorageAdapter s3StorageAdapter;
    //private final FFmpegFrameExtractor frameExtractor;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    private static final String[] SUPPORTED_FORMATS = {"mp4", "avi", "mov", "mkv"};
    private static final int DEFAULT_MAX_FRAMES = 100;
    private static final double DEFAULT_INTERVAL = 1.0;

    /*public String processVideo(VideoMessage videoMessage) {
        String videoId = videoMessage.getIdVideoSend();
        String fileName = videoMessage.getNmVideo();
        String videoPath = videoMessage.getNmVideoPathOrigin();

        log.info("=== INICIANDO PROCESSAMENTO DE FRAMES ===");
        log.info("Vídeo: {}", fileName);
        log.info("ID: {}", videoId);

        try {
            validateVideoFormat(fileName);

            if (!s3StorageAdapter.videoExists(videoPath)) {
                throw new VideoProcessingException("Vídeo não encontrado no S3: " + videoId);
            }

            validateVideoSize(videoPath);

            log.info("Fazendo download do vídeo do S3...");
            byte[] videoData = s3StorageAdapter.downloadVideo(videoPath);
            log.info("Download concluído. Tamanho: {} bytes", videoData.length);

            log.info("Extraindo frames do vídeo...");
            byte[] framesZip = frameExtractor.extractFramesToZip(
                videoData,
                fileName,
                DEFAULT_INTERVAL,
                DEFAULT_MAX_FRAMES
            );
            log.info("Extração concluída. ZIP gerado: {} bytes", framesZip.length);

            log.info("Fazendo upload do ZIP de frames para S3...");
            String zipPath = s3StorageAdapter.uploadFramesZip(videoId, framesZip);
            log.info("Upload concluído. Caminho: {}", zipPath);

            log.info("=== PROCESSAMENTO CONCLUÍDO COM SUCESSO ===");
            return zipPath;
        } catch (Exception e) {
            log.error("=== ERRO NO PROCESSAMENTO ===");
            log.error("Erro ao processar vídeo {}: {}", videoId, e.getMessage(), e);
            throw new VideoProcessingException("Falha no processamento: " + e.getMessage(), e);
        }
    }

    private void validateVideoFormat(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new VideoProcessingException("Nome do arquivo não informado");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        boolean isSupported = false;

        for (String format : SUPPORTED_FORMATS) {
            if (format.equals(extension)) {
                isSupported = true;
                break;
            }
        }

        if (!isSupported) {
            throw new VideoProcessingException(
                String.format("Formato %s não suportado. Formatos aceitos: %s",
                    extension, String.join(", ", SUPPORTED_FORMATS))
            );
        }

        log.info("Formato de vídeo validado: {}", extension);
    }

    private void validateVideoSize(String videoPath) {
        try {
            var metadata = s3StorageAdapter.getVideoMetadata(videoPath);
            long fileSize = metadata.getSize();

            if (fileSize > MAX_FILE_SIZE) {
                throw new VideoProcessingException(
                    String.format("Arquivo muito grande: %d bytes. Máximo permitido: %d bytes",
                        fileSize, MAX_FILE_SIZE)
                );
            }

            log.info("Tamanho do vídeo validado: {} bytes", fileSize);

        } catch (Exception e) {
            log.warn("Não foi possível validar tamanho do vídeo: {}", e.getMessage());
            // RN3: Em caso de falha na validação, continua processamento mas notifica
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }

    public static class VideoProcessingException extends RuntimeException {
        public VideoProcessingException(String message) {
            super(message);
        }

        public VideoProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }*/
}
