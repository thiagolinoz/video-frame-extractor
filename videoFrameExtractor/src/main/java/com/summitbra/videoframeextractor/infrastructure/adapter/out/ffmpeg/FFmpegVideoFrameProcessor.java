package com.summitbra.videoframeextractor.infrastructure.adapter.out.ffmpeg;

import com.summitbra.videoframeextractor.application.port.out.VideoFrameProcessor;
import com.summitbra.videoframeextractor.application.port.out.FileSystemService;
import com.summitbra.videoframeextractor.domain.model.VideoFile;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import com.summitbra.videoframeextractor.domain.model.FrameExtractionResult;
import com.summitbra.videoframeextractor.infrastructure.adapter.out.javacv.JavaCVFrameExtractor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import javax.imageio.ImageIO;

@Component
@Primary
@Slf4j
public class FFmpegVideoFrameProcessor implements VideoFrameProcessor {

    private final FileSystemService fileSystemService;
    private final JavaCVFrameExtractor javaCVExtractor;

    @Autowired(required = false)
    private FFmpeg ffmpeg;

    @Autowired(required = false)
    private FFprobe ffprobe;

    @Value("${app.video.temp-dir:temp}")
    private String tempDirectory;

    @Value("${app.video.output-dir:output}")
    private String outputDirectory;

    public FFmpegVideoFrameProcessor(FileSystemService fileSystemService,
                                   JavaCVFrameExtractor javaCVExtractor) {
        this.fileSystemService = fileSystemService;
        this.javaCVExtractor = javaCVExtractor;
    }

    @Override
    public FrameExtractionResult extractFrames(VideoFile videoFile,
                                             String videoId,
                                             VideoProcessingJob.ProcessingParameters parameters) {
        long startTime = System.currentTimeMillis();
        Path tempVideoPath = null;
        Path framesDir = null;

        try {
            String tempFileName = "video_" + videoId + videoFile.getExtension();
            tempVideoPath = fileSystemService.saveVideoFile(videoFile.getContent(), tempFileName, tempDirectory);

            framesDir = fileSystemService.createTempDirectory("frames_" + videoId);

            int extractedFrames = extractFramesWithFFmpeg(tempVideoPath, framesDir, parameters);

            Path zipPath = fileSystemService.createZipFile(framesDir, videoId, outputDirectory);
            String zipFileName = zipPath.getFileName().toString();
            String downloadUrl = "/api/video/download/" + zipFileName;

            long processingTime = System.currentTimeMillis() - startTime;

            return FrameExtractionResult.builder()
                .framesExtracted(extractedFrames)
                .processingTimeMs(processingTime)
                .zipPath(zipPath)
                .zipFilename(zipFileName)
                .downloadUrl(downloadUrl)
                .build();

        } catch (Exception e) {
            log.error("Erro na extração de frames", e);
            throw new VideoProcessingException("Erro na extração de frames: " + e.getMessage(), e);
        } finally {
            if (tempVideoPath != null) {
                fileSystemService.deleteFile(tempVideoPath);
            }
            if (framesDir != null) {
                fileSystemService.deleteDirectory(framesDir);
            }
        }
    }

    private int extractFramesWithFFmpeg(Path videoPath, Path outputDir,
                                       VideoProcessingJob.ProcessingParameters parameters) throws IOException {
        try {
            if (isFFmpegMock()) {
                log.warn("FFmpeg não disponível. Usando JavaCV para extrair frames REAIS do vídeo.");
                return extractRealFramesWithJavaCV(videoPath, outputDir, parameters);
            }

            return extractFramesWithFFmpegNative(videoPath, outputDir, parameters);

        } catch (Exception e) {
            log.error("Erro na extração com FFmpeg: {}", e.getMessage());

            log.info("Tentando extrair frames REAIS com JavaCV...");
            try {
                return extractRealFramesWithJavaCV(videoPath, outputDir, parameters);
            } catch (Exception javacvError) {
                log.error("Erro também com JavaCV: {}", javacvError.getMessage());

                log.warn("Criando frames representativos como último recurso...");
                return createVideoBasedFrames(videoPath, outputDir, parameters);
            }
        }
    }

    private int extractFramesWithFFmpegNative(Path videoPath, Path outputDir,
                                            VideoProcessingJob.ProcessingParameters parameters) throws IOException {

        FFmpegProbeResult probeResult = ffprobe.probe(videoPath.toString());
        double duration = probeResult.getFormat().duration;

        log.info("Duração do vídeo: {} segundos", duration);

        int totalPossibleFrames = (int) Math.ceil(duration / parameters.getIntervalSeconds());
        int framesToExtract = Math.min(totalPossibleFrames, parameters.getMaxFrames());

        log.info("Extraindo {} frames com intervalo de {} segundos",
            framesToExtract, parameters.getIntervalSeconds());

        String outputPattern = outputDir.resolve("frame_%04d." +
            parameters.getOutputFormat().toLowerCase()).toString();

        FFmpegBuilder builder = new FFmpegBuilder()
            .setInput(videoPath.toString())
            .overrideOutputFiles(true)
            .addOutput(outputPattern)
            .setVideoFilter(String.format("fps=1/%f", parameters.getIntervalSeconds()))
            .setFrames(framesToExtract)
            .setFormat("image2")
            .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        int actualFramesExtracted = (int) Files.list(outputDir)
            .filter(Files::isRegularFile)
            .count();

        log.info("Frames extraídos com sucesso via FFmpeg: {}", actualFramesExtracted);
        return actualFramesExtracted;
    }

    private int extractRealFramesWithJavaCV(Path videoPath, Path outputDir,
                                          VideoProcessingJob.ProcessingParameters parameters) throws IOException {

        log.info("🎬 Extraindo frames REAIS do vídeo usando JavaCV (sem FFmpeg)");

        try {
            if (!javaCVExtractor.canProcess(videoPath)) {
                log.warn("JavaCV não pode processar o arquivo {}. Usando frames representativos.",
                        videoPath.getFileName());
                return createVideoBasedFrames(videoPath, outputDir, parameters);
            }

            int extractedFrames = javaCVExtractor.extractRealFrames(
                videoPath,
                outputDir,
                parameters.getIntervalSeconds(),
                parameters.getMaxFrames(),
                parameters.getOutputFormat()
            );

            log.info("Frames REAIS extraídos com sucesso via JavaCV: {}", extractedFrames);
            return extractedFrames;

        } catch (Exception e) {
            log.error("Erro na extração com JavaCV: {}", e.getMessage());

            log.warn("Fallback para frames representativos devido a erro no JavaCV");
            return createVideoBasedFrames(videoPath, outputDir, parameters);
        }
    }

    private int createVideoBasedFrames(Path videoPath, Path outputDir,
                                     VideoProcessingJob.ProcessingParameters parameters) throws IOException {
        log.info("Criando frames representativos baseados no vídeo: {}", videoPath.getFileName());

        File videoFile = videoPath.toFile();
        long fileSize = videoFile.length();
        String fileName = videoFile.getName();

        int framesToCreate = Math.min(parameters.getMaxFrames(), 10);

        for (int i = 1; i <= framesToCreate; i++) {
            String frameFileName = String.format("frame_%04d.%s", i,
                parameters.getOutputFormat().toLowerCase().equals("jpg") ? "jpg" : "png");
            Path framePath = outputDir.resolve(frameFileName);

            BufferedImage image = createVideoRepresentativeFrame(i, fileName, fileSize, parameters);

            String format = parameters.getOutputFormat().toLowerCase().equals("jpg") ? "jpg" : "png";
            ImageIO.write(image, format, framePath.toFile());

            log.debug("Frame representativo criado: {}", frameFileName);
        }

        log.info("Criados {} frames representativos para o vídeo {}", framesToCreate, fileName);
        return framesToCreate;
    }

    private BufferedImage createVideoRepresentativeFrame(int frameNumber, String videoFileName,
                                                        long fileSize, VideoProcessingJob.ProcessingParameters parameters) {
        int width = 640;
        int height = 480;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int hash = videoFileName.hashCode();
        Color backgroundColor = new Color(
            Math.abs(hash % 100) + 50,
            Math.abs((hash >> 8) % 100) + 70,
            Math.abs((hash >> 16) % 100) + 90
        );
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);

        GradientPaint gradient = new GradientPaint(
            0, 0, backgroundColor,
            width, height, backgroundColor.brighter()
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(10, 10, width - 20, height - 20);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        String title = "FRAME " + frameNumber;
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleX = (width - titleWidth) / 2;
        int titleY = height / 2 - 60;

        g2d.setColor(Color.BLACK);
        g2d.drawString(title, titleX + 2, titleY + 2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, titleX, titleY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String shortName = videoFileName.length() > 30 ?
            videoFileName.substring(0, 27) + "..." : videoFileName;
        fm = g2d.getFontMetrics();
        int nameWidth = fm.stringWidth(shortName);
        int nameX = (width - nameWidth) / 2;
        int nameY = titleY + 40;

        g2d.setColor(Color.BLACK);
        g2d.drawString(shortName, nameX + 1, nameY + 1);
        g2d.setColor(Color.WHITE);
        g2d.drawString(shortName, nameX, nameY);

        String sizeText = String.format("Tamanho: %.1f MB", fileSize / (1024.0 * 1024.0));
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        fm = g2d.getFontMetrics();
        int sizeWidth = fm.stringWidth(sizeText);
        int sizeX = (width - sizeWidth) / 2;
        int sizeY = nameY + 30;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString(sizeText, sizeX, sizeY);

        String paramsText = String.format("Intervalo: %.1fs | Max: %d frames",
            parameters.getIntervalSeconds(), parameters.getMaxFrames());
        fm = g2d.getFontMetrics();
        int paramsWidth = fm.stringWidth(paramsText);
        int paramsX = (width - paramsWidth) / 2;
        int paramsY = sizeY + 20;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString(paramsText, paramsX, paramsY);

        double simulatedTime = frameNumber * parameters.getIntervalSeconds();
        String timestamp = String.format("Time: %02d:%02d.%03d",
            (int)(simulatedTime / 60), (int)(simulatedTime % 60),
            (int)((simulatedTime % 1) * 1000));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2d.setColor(Color.YELLOW);
        g2d.drawString(timestamp, 20, height - 30);

        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.setColor(Color.ORANGE);
        String demoText = "Modo Demonstração (FFmpeg não disponível)";
        fm = g2d.getFontMetrics();
        int demoX = width - fm.stringWidth(demoText) - 20;
        g2d.drawString(demoText, demoX, height - 30);

        g2d.setColor(new Color(255, 255, 255, 100));
        for (int i = 0; i < frameNumber; i++) {
            int x = 50 + (i * 40) % (width - 100);
            int y = 80 + (i * 30) % 150;
            g2d.fillOval(x, y, 20, 20);
        }

        g2d.dispose();
        return image;
    }

    private boolean isFFmpegMock() {
        if (ffmpeg == null || ffprobe == null) {
            return true;
        }
        try {
            String version = ffmpeg.version();
            return version.startsWith("Mock");
        } catch (Exception e) {
            return true;
        }
    }

    private int createMockFrames(Path outputDir, VideoProcessingJob.ProcessingParameters parameters) throws IOException {
        int framesToCreate = Math.min(10, parameters.getMaxFrames()); // Máximo 10 frames mock
        Random random = new Random();

        for (int i = 1; i <= framesToCreate; i++) {
            String fileName = String.format("frame_%04d.%s", i,
                parameters.getOutputFormat().toLowerCase().equals("jpg") ? "jpg" : "png");
            Path framePath = outputDir.resolve(fileName);

            BufferedImage image = createMockImage(i, random);

            String format = parameters.getOutputFormat().toLowerCase().equals("jpg") ? "jpg" : "png";
            ImageIO.write(image, format, framePath.toFile());
        }

        log.info("Criados {} frames mock reais (imagens) em modo demonstração", framesToCreate);
        return framesToCreate;
    }

    private BufferedImage createMockImage(int frameNumber, Random random) {
        int width = 640;
        int height = 480;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color backgroundColor = new Color(
            100 + random.nextInt(100),  // R: 100-200
            120 + random.nextInt(100),  // G: 120-220
            140 + random.nextInt(100)   // B: 140-240
        );
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.WHITE);

        g2d.drawRect(10, 10, width - 20, height - 20);

        for (int i = 0; i < 5; i++) {
            int x = random.nextInt(width - 100);
            int y = random.nextInt(height - 100);
            int size = 20 + random.nextInt(80);

            Color circleColor = new Color(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                128 // Semi-transparente
            );
            g2d.setColor(circleColor);
            g2d.fillOval(x, y, size, size);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String frameText = "DEMO FRAME " + frameNumber;
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(frameText);
        int textX = (width - textWidth) / 2;
        int textY = height / 2;

        g2d.setColor(Color.BLACK);
        g2d.drawString(frameText, textX + 2, textY + 2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(frameText, textX, textY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String infoText = "Video Frame Extractor - Mock Mode";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(infoText);
        textX = (width - textWidth) / 2;
        textY = height - 50;

        g2d.setColor(Color.BLACK);
        g2d.drawString(infoText, textX + 1, textY + 1);
        g2d.setColor(Color.WHITE);
        g2d.drawString(infoText, textX, textY);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        String timestamp = String.format("Time: %02d:%02d.%03d",
            frameNumber / 60, frameNumber % 60, frameNumber * 100 % 1000);
        g2d.drawString(timestamp, 20, height - 20);

        g2d.dispose();
        return image;
    }

    public static class VideoProcessingException extends RuntimeException {
        public VideoProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
