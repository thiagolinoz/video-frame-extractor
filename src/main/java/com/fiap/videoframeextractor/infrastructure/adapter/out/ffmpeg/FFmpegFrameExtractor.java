package com.fiap.videoframeextractor.infrastructure.adapter.out.ffmpeg;

import com.fiap.videoframeextractor.domain.exceptions.FrameExtractionException;
import com.fiap.videoframeextractor.domain.ports.out.FrameExtractorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class FFmpegFrameExtractor implements FrameExtractorPort {

    @Override
    public byte[] extractFramesToZip(byte[] videoData, String fileName, double intervalSeconds, int maxFrames) {
        log.info("Iniciando extração de frames: arquivo={}, intervalo={}s, maxFrames={}",
                fileName, intervalSeconds, maxFrames);

        Path tempVideoPath = null;
        List<BufferedImage> extractedFrames;

        try {
            tempVideoPath = createTempVideoFile(videoData, fileName);

            extractedFrames = extractFrames(tempVideoPath, intervalSeconds, maxFrames);

            byte[] zipData = createFramesZip(extractedFrames, fileName);

            log.info("Extração concluída com sucesso: {} frames extraídos, ZIP de {} bytes",
                    extractedFrames.size(), zipData.length);

            return zipData;

        } catch (Exception e) {
            log.error("Erro na extração de frames: {}", e.getMessage(), e);
            throw new FrameExtractionException("Falha na extração de frames: " + e.getMessage(), e);
        } finally {
            // Cleanup
            cleanupTempFile(tempVideoPath);
        }
    }

    private Path createTempVideoFile(byte[] videoData, String fileName) throws IOException {
        String extension = getFileExtension(fileName);
        Path tempPath = Files.createTempFile("video_", "." + extension);

        try (FileOutputStream fos = new FileOutputStream(tempPath.toFile())) {
            fos.write(videoData);
        }

        log.debug("Arquivo temporário criado: {}", tempPath);
        return tempPath;
    }

    private List<BufferedImage> extractFrames(Path videoPath, double intervalSeconds, int maxFrames) throws Exception {
        List<BufferedImage> frames = new ArrayList<>();

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath.toString());
             Java2DFrameConverter converter = new Java2DFrameConverter()) {

            grabber.start();

            double frameRate = grabber.getFrameRate();
            int frameInterval = (int) Math.max(1, frameRate * intervalSeconds);

            log.debug("Vídeo info: frameRate={}, frameInterval={}, duração={}s",
                    frameRate, frameInterval, grabber.getLengthInTime() / 1000000.0);

            Frame frame;
            int frameCount = 0;
            int extractedCount = 0;

            while ((frame = grabber.grabFrame()) != null && extractedCount < maxFrames) {
                if (frame.image != null && frameCount % frameInterval == 0) {
                    BufferedImage bufferedImage = converter.getBufferedImage(frame);
                    if (bufferedImage != null) {
                        // Criar cópia profunda do BufferedImage para evitar sobrescrita
                        BufferedImage copy = deepCopy(bufferedImage);
                        frames.add(copy);
                        extractedCount++;
                        log.debug("Frame extraído: {}/{}", extractedCount, maxFrames);
                    }
                }
                frameCount++;
            }

            log.info("Frames processados: total={}, extraídos={}", frameCount, extractedCount);
        }

        return frames;
    }

    private byte[] createFramesZip(List<BufferedImage> frames, String originalFileName) throws IOException {
        String baseName = getFileNameWithoutExtension(originalFileName);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (int i = 0; i < frames.size(); i++) {
                String frameName = String.format("%s_frame_%03d.jpg", baseName, i + 1);
                ZipEntry entry = new ZipEntry(frameName);
                zos.putNextEntry(entry);

                ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
                ImageIO.write(frames.get(i), "jpg", imageBytes);
                zos.write(imageBytes.toByteArray());

                zos.closeEntry();
                log.debug("Frame adicionado ao ZIP: {}", frameName);
            }

            zos.finish();
            return baos.toByteArray();
        }
    }

    private void cleanupTempFile(Path tempPath) {
        if (tempPath != null) {
            try {
                Files.deleteIfExists(tempPath);
                log.debug("Arquivo temporário removido: {}", tempPath);
            } catch (IOException e) {
                log.warn("Falha ao remover arquivo temporário: {}", e.getMessage());
            }
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "mp4";
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDot);
    }

    /**
     * Cria uma cópia profunda do BufferedImage para evitar que o FFmpeg
     * sobrescreva o buffer interno entre frames
     */
    private BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
            source.getWidth(),
            source.getHeight(),
            source.getType()
        );
        
        java.awt.Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        
        return copy;
    }
}
