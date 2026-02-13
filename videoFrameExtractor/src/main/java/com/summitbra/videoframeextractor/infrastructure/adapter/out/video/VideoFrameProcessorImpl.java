package com.summitbra.videoframeextractor.infrastructure.adapter.out.video;

import com.summitbra.videoframeextractor.application.port.out.VideoFrameProcessor;
import com.summitbra.videoframeextractor.domain.model.VideoFile;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import com.summitbra.videoframeextractor.domain.model.FrameExtractionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Component("myVideoFrameProcessor")
@Slf4j
public class VideoFrameProcessorImpl implements VideoFrameProcessor {

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Override
    public FrameExtractionResult extractFrames(VideoFile videoFile, String videoId, VideoProcessingJob.ProcessingParameters parameters) {
        long startTime = System.currentTimeMillis();

        try {
            Path tempVideoPath = Files.createTempFile("video_" + videoId, getFileExtension(videoFile.getFilename()));
            Files.write(tempVideoPath, videoFile.getContent());

            Path outputDir = tempVideoPath.getParent().resolve("frames_" + videoId);
            Files.createDirectories(outputDir);

            extractFramesWithFFmpeg(tempVideoPath, outputDir);

            long frameCount = Files.list(outputDir)
                    .filter(path -> path.toString().toLowerCase().endsWith(".png"))
                    .count();

            long processingTime = System.currentTimeMillis() - startTime;

            return FrameExtractionResult.builder()
                    .framesExtracted((int) frameCount)
                    .processingTimeMs(processingTime)
                    .zipPath(outputDir)
                    .zipFilename("frames_" + videoId + "_" + Instant.now().getEpochSecond() + ".zip")
                    .downloadUrl("/download/frames/" + videoId)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao extrair frames do vídeo {}: {}", videoId, e.getMessage(), e);
            long processingTime = System.currentTimeMillis() - startTime;

            return FrameExtractionResult.builder()
                    .framesExtracted(0)
                    .processingTimeMs(processingTime)
                    .zipPath(null)
                    .zipFilename(null)
                    .downloadUrl(null)
                    .build();
        }
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".mp4";
    }

    public void extractFramesWithFFmpeg(Path videoFile, Path outputDir) throws IOException, InterruptedException {
        String[] command = {
            ffmpegPath,
            "-i", videoFile.toString(),
            "-vf", "fps=1",
            "-y",
            outputDir.resolve("frame_%04d.png").toString()
        };

        log.info("Executando comando FFmpeg: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("FFmpeg: {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg falhou com código: " + exitCode);
        }

        log.info("Extração de frames concluída com sucesso");
    }
}
