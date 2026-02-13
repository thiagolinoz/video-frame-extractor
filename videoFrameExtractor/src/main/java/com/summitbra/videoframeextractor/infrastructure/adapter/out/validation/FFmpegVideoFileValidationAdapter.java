package com.summitbra.videoframeextractor.infrastructure.adapter.out.validation;

import com.summitbra.videoframeextractor.application.port.out.VideoFileValidationPort;
import com.summitbra.videoframeextractor.domain.model.VideoFile;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

@Component
@Slf4j
public class FFmpegVideoFileValidationAdapter implements VideoFileValidationPort {

    @Autowired(required = false)
    private FFprobe ffprobe;

    @Value("${app.video.temp-dir:temp}")
    private String tempDir;

    private static final Set<String> SUPPORTED_FORMATS = Set.of(
        "video/mp4", "video/avi", "video/mov", "video/quicktime",
        "video/x-msvideo", "video/webm", "video/mkv", "video/x-matroska"
    );

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15MB

    @Override
    public boolean isValidVideoFile(VideoFile videoFile) {
        try {
            if (videoFile.getSize() > MAX_FILE_SIZE) {
                throw new VideoValidationException("Arquivo muito grande. Máximo permitido: 15MB");
            }

            if (!isSupportedFormat(videoFile.getContentType())) {
                throw new VideoValidationException("Formato não suportado: " + videoFile.getContentType());
            }

            if (isFFmpegMock()) {
                log.warn("FFmpeg não disponível. Fazendo apenas validação básica de arquivo.");
                return true;
            }

            Path tempFile = saveTemporaryFile(videoFile);
            try {
                FFmpegProbeResult probeResult = ffprobe.probe(tempFile.toString());

                boolean hasVideoStream = probeResult.getStreams().stream()
                    .anyMatch(stream -> "video".equals(stream.codec_type.toString()));

                if (!hasVideoStream) {
                    throw new VideoValidationException("Arquivo não contém stream de vídeo válido");
                }

                return true;

            } finally {
                Files.deleteIfExists(tempFile);
            }

        } catch (IOException e) {
            log.error("Erro ao validar arquivo de vídeo", e);
            throw new VideoValidationException("Erro ao validar arquivo de vídeo", e);
        }
    }

    @Override
    public boolean isSupportedFormat(String contentType) {
        return contentType != null && SUPPORTED_FORMATS.contains(contentType.toLowerCase());
    }

    @Override
    public VideoMetadata getVideoMetadata(VideoFile videoFile) {
        try {
            if (isFFmpegMock()) {
                log.warn("FFmpeg não disponível. Retornando metadados mock.");
                return new VideoMetadata(60.0, 1920, 1080, "mock", 30.0);
            }

            Path tempFile = saveTemporaryFile(videoFile);
            try {
                FFmpegProbeResult probeResult = ffprobe.probe(tempFile.toString());

                FFmpegFormat format = probeResult.getFormat();
                FFmpegStream videoStream = probeResult.getStreams().stream()
                    .filter(stream -> "video".equals(stream.codec_type.toString()))
                    .findFirst()
                    .orElseThrow(() -> new VideoValidationException("Nenhum stream de vídeo encontrado"));

                return new VideoMetadata(
                    format.duration,
                    videoStream.width,
                    videoStream.height,
                    videoStream.codec_name,
                    videoStream.avg_frame_rate.doubleValue()
                );

            } finally {
                Files.deleteIfExists(tempFile);
            }

        } catch (IOException e) {
            log.error("Erro ao obter metadados do vídeo", e);
            throw new VideoValidationException("Erro ao obter metadados do vídeo", e);
        }
    }

    private boolean isFFmpegMock() {
        if (ffprobe == null) {
            return true;
        }
        try {
            String version = ffprobe.version();
            return version.startsWith("Mock");
        } catch (Exception e) {
            return true;
        }
    }

    private Path saveTemporaryFile(VideoFile videoFile) throws IOException {
        Path tempPath = Path.of(tempDir);
        Files.createDirectories(tempPath);

        Path tempFile = tempPath.resolve("temp_" + System.nanoTime() + "_" + videoFile.getFilename());
        Files.write(tempFile, videoFile.getContent(), StandardOpenOption.CREATE);

        return tempFile;
    }
}
