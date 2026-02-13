package com.summitbra.videoframeextractor.application.usecase;

import com.summitbra.videoframeextractor.application.port.in.ProcessVideoFramesUseCase;
import com.summitbra.videoframeextractor.application.port.out.VideoProcessingRepository;
import com.summitbra.videoframeextractor.application.port.out.VideoFrameProcessor;
import com.summitbra.videoframeextractor.domain.model.VideoFile;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import com.summitbra.videoframeextractor.domain.model.FrameExtractionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessVideoFramesService implements ProcessVideoFramesUseCase {

    private final VideoProcessingRepository repository;
    private final VideoFrameProcessor frameProcessor;

    @Override
    @Transactional
    public VideoProcessingJob processVideo(VideoFile videoFile, ProcessVideoCommand command) {
        String videoId = generateVideoId(videoFile);

        Optional<VideoProcessingJob> existingJob = repository.findByVideoId(videoId);
        if (existingJob.isPresent()) {
            VideoProcessingJob job = existingJob.get();

            if (job.getStatus() == VideoProcessingJob.ProcessingStatus.COMPLETED) {
                log.info("Job já processado com sucesso para vídeo: {} (ID: {})",
                    videoFile.getFilename(), videoId);
                return job;
            }

            if (job.getStatus() == VideoProcessingJob.ProcessingStatus.ERROR) {
                log.info("Reprocessando vídeo que estava em erro: {} (ID: {})",
                    videoFile.getFilename(), videoId);
                job = job.withStatus(VideoProcessingJob.ProcessingStatus.PROCESSING);
                job = repository.save(job);
            }

            if (job.getStatus() == VideoProcessingJob.ProcessingStatus.PROCESSING) {
                log.info("Job já em processamento para vídeo: {} (ID: {})",
                    videoFile.getFilename(), videoId);
                return job;
            }
        }

        VideoProcessingJob job = VideoProcessingJob.builder()
            .videoId(videoId)
            .originalFilename(videoFile.getFilename())
            .fileSize(videoFile.getSize())
            .parameters(VideoProcessingJob.ProcessingParameters.builder()
                .intervalSeconds(command.intervalSeconds())
                .maxFrames(command.maxFrames())
                .outputFormat(command.outputFormat())
                .build())
            .status(VideoProcessingJob.ProcessingStatus.PROCESSING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        job = repository.save(job);

        try {
            log.info("Iniciando processamento do vídeo: {} (ID: {})",
                videoFile.getFilename(), videoId);

            FrameExtractionResult result = frameProcessor.extractFrames(videoFile, videoId, job.getParameters());

            VideoProcessingJob.ProcessingResult processResult = VideoProcessingJob.ProcessingResult.builder()
                .framesExtracted(result.getFramesExtracted())
                .processingTimeMs(result.getProcessingTimeMs())
                .zipFilename(result.getZipFilename())
                .downloadUrl(result.getDownloadUrl())
                .build();

            job = job.withResult(processResult);
            job = repository.save(job);

            log.info("Processamento concluído. {} frames extraídos em {}ms",
                result.getFramesExtracted(), result.getProcessingTimeMs());

            return job;

        } catch (Exception e) {
            log.error("Erro ao processar vídeo: ", e);

            job = job.withError(e.getMessage());
            job = repository.save(job);

            throw new VideoProcessingException("Erro ao processar vídeo: " + e.getMessage(), e);
        }
    }

    private String generateVideoId(VideoFile videoFile) {
        String input = videoFile.getFilename() + "_" + videoFile.getSize();
        return UUID.nameUUIDFromBytes(input.getBytes()).toString();
    }

    public static class VideoProcessingException extends RuntimeException {
        public VideoProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
