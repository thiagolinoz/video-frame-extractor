package com.summitbra.videoframeextractor.infrastructure.adapter.out.javacv;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;


@Component
@Slf4j
public class JavaCVFrameExtractor {


    public int extractRealFrames(Path videoPath, Path outputDir, double intervalSeconds,
                               int maxFrames, String outputFormat) {

        log.info("Extraindo frames REAIS do vídeo: {}", videoPath.getFileName());

        FFmpegFrameGrabber grabber = null;
        Java2DFrameConverter converter = new Java2DFrameConverter();

        try {
            grabber = new FFmpegFrameGrabber(videoPath.toFile());
            grabber.start();

            double frameRate = grabber.getFrameRate();
            int totalFrames = grabber.getLengthInFrames();
            double duration = totalFrames / frameRate;

            log.info("Vídeo: {} fps, {} frames totais, {:.1f}s duração",
                    frameRate, totalFrames, duration);

            int frameInterval = (int) Math.max(1, intervalSeconds * frameRate);
            int framesToExtract = Math.min(maxFrames, totalFrames / frameInterval);

            log.info("Extraindo {} frames com intervalo de {:.1f}s", framesToExtract, intervalSeconds);

            int extractedCount = 0;
            int currentFrameIndex = 0;

            while (extractedCount < framesToExtract && currentFrameIndex < totalFrames) {
                try {
                    grabber.setFrameNumber(currentFrameIndex);
                    Frame frame = grabber.grabImage();

                    if (frame != null) {
                        BufferedImage bufferedImage = converter.getBufferedImage(frame);

                        if (bufferedImage != null) {
                            String fileName = String.format("frame_%04d.%s",
                                    extractedCount + 1, outputFormat.toLowerCase());
                            Path framePath = outputDir.resolve(fileName);

                            String format = outputFormat.toLowerCase().equals("jpg") ? "jpg" : "png";
                            ImageIO.write(bufferedImage, format, framePath.toFile());

                            extractedCount++;
                            log.debug("Frame {} extraído: {} (tempo: {:.1f}s)",
                                    extractedCount, fileName, currentFrameIndex / frameRate);
                        }
                    }

                    currentFrameIndex += frameInterval;

                } catch (Exception e) {
                    log.warn("⚠Erro ao extrair frame {}: {}", currentFrameIndex, e.getMessage());
                    currentFrameIndex += frameInterval;
                }
            }

            log.info("Extração concluída: {} frames REAIS extraídos do vídeo!", extractedCount);
            return extractedCount;

        } catch (Exception e) {
            log.error("Erro na extração de frames: {}", e.getMessage());
            throw new VideoFrameExtractionException("Erro ao extrair frames reais: " + e.getMessage(), e);

        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception e) {
                    log.warn("Erro ao liberar recursos: {}", e.getMessage());
                }
            }
        }
    }

    public boolean canProcess(Path videoPath) {
        try {
            String fileName = videoPath.getFileName().toString().toLowerCase();

            return fileName.endsWith(".mp4") ||
                   fileName.endsWith(".avi") ||
                   fileName.endsWith(".mov") ||
                   fileName.endsWith(".mkv") ||
                   fileName.endsWith(".wmv") ||
                   fileName.endsWith(".flv") ||
                   fileName.endsWith(".webm") ||
                   fileName.endsWith(".m4v");

        } catch (Exception e) {
            log.warn("⚠️ Erro ao verificar suporte para arquivo {}: {}", videoPath, e.getMessage());
            return false;
        }
    }


    public VideoInfo getVideoInfo(Path videoPath) throws IOException {
        FFmpegFrameGrabber grabber = null;

        try {
            grabber = new FFmpegFrameGrabber(videoPath.toFile());
            grabber.start();

            double frameRate = grabber.getFrameRate();
            int totalFrames = grabber.getLengthInFrames();
            double duration = totalFrames / frameRate;
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();

            return new VideoInfo(duration, frameRate, totalFrames, width, height);

        } catch (Exception e) {
            throw new IOException("Erro ao obter informações do vídeo: " + e.getMessage(), e);
        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception e) {
                    log.warn("⚠️ Erro ao liberar recursos: {}", e.getMessage());
                }
            }
        }
    }

    public static class VideoInfo {
        public final double duration;
        public final double frameRate;
        public final int totalFrames;
        public final int width;
        public final int height;

        public VideoInfo(double duration, double frameRate, int totalFrames, int width, int height) {
            this.duration = duration;
            this.frameRate = frameRate;
            this.totalFrames = totalFrames;
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return String.format("VideoInfo{duration=%.1fs, fps=%.1f, frames=%d, resolution=%dx%d}",
                    duration, frameRate, totalFrames, width, height);
        }
    }

    public static class VideoFrameExtractionException extends RuntimeException {
        public VideoFrameExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
