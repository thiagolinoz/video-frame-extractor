package com.summitbra.videoframeextractor.infrastructure.adapter.out.persistence.mapper;

import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingConstants;
import com.summitbra.videoframeextractor.infrastructure.adapter.out.persistence.entity.VideoProcessingEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VideoProcessingPersistenceMapper {

    public VideoProcessingEntity toEntity(VideoProcessingJob job) {
        VideoProcessingEntity.VideoProcessingEntityBuilder builder = VideoProcessingEntity.builder()
            .videoId(job.getVideoId())
            .originalFilename(job.getOriginalFilename())
            .fileSize(job.getFileSize())
            .status(mapStatusToEntity(job.getStatus()))
            .errorMessage(job.getErrorMessage())
            .createdAt(job.getCreatedAt())
            .updatedAt(job.getUpdatedAt());

        mapResultToEntityBuilder(job.getResult(), builder);
        mapParametersToEntityBuilder(job.getParameters(), builder);

        return builder.build();
    }

    public void updateEntity(VideoProcessingEntity entity, VideoProcessingJob job) {
        entity.setOriginalFilename(job.getOriginalFilename());
        entity.setFileSize(job.getFileSize());
        entity.setStatus(mapStatusToEntity(job.getStatus()));
        entity.setErrorMessage(job.getErrorMessage());
        entity.setUpdatedAt(job.getUpdatedAt());

        updateEntityWithResult(entity, job.getResult());
        updateEntityWithParameters(entity, job.getParameters());
    }

    public VideoProcessingJob toDomain(VideoProcessingEntity entity) {
        return VideoProcessingJob.builder()
            .videoId(entity.getVideoId())
            .originalFilename(entity.getOriginalFilename())
            .fileSize(entity.getFileSize())
            .parameters(mapParametersToDomain(entity))
            .status(mapStatusToDomain(entity.getStatus()))
            .errorMessage(entity.getErrorMessage())
            .result(mapResultToDomain(entity))
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    private void mapResultToEntityBuilder(VideoProcessingJob.ProcessingResult result,
                                          VideoProcessingEntity.VideoProcessingEntityBuilder builder) {
        Optional.ofNullable(result)
            .ifPresentOrElse(
                r -> builder
                    .framesExtracted(r.getFramesExtracted())
                    .processingTimeMs(r.getProcessingTimeMs())
                    .zipFilename(r.getZipFilename())
                    .downloadUrl(r.getDownloadUrl()),
                () -> builder
                    .framesExtracted(null)
                    .processingTimeMs(null)
                    .zipFilename(null)
                    .downloadUrl(null)
            );
    }

    private void mapParametersToEntityBuilder(VideoProcessingJob.ProcessingParameters parameters,
                                              VideoProcessingEntity.VideoProcessingEntityBuilder builder) {
        Optional.ofNullable(parameters)
            .ifPresentOrElse(
                p -> builder
                    .intervalSeconds(p.getIntervalSeconds())
                    .maxFrames(p.getMaxFrames())
                    .outputFormat(p.getOutputFormat()),
                () -> builder
                    .intervalSeconds(null)
                    .maxFrames(null)
                    .outputFormat(null)
            );
    }

    private void updateEntityWithResult(VideoProcessingEntity entity, VideoProcessingJob.ProcessingResult result) {
        if (result != null) {
            entity.setFramesExtracted(result.getFramesExtracted());
            entity.setProcessingTimeMs(result.getProcessingTimeMs());
            entity.setZipFilename(result.getZipFilename());
            entity.setDownloadUrl(result.getDownloadUrl());
        } else {
            clearEntityResult(entity);
        }
    }

    private void updateEntityWithParameters(VideoProcessingEntity entity, VideoProcessingJob.ProcessingParameters parameters) {
        if (parameters != null) {
            entity.setIntervalSeconds(parameters.getIntervalSeconds());
            entity.setMaxFrames(parameters.getMaxFrames());
            entity.setOutputFormat(parameters.getOutputFormat());
        } else {
            clearEntityParameters(entity);
        }
    }

    private void clearEntityResult(VideoProcessingEntity entity) {
        entity.setFramesExtracted(null);
        entity.setProcessingTimeMs(null);
        entity.setZipFilename(null);
        entity.setDownloadUrl(null);
    }

    private void clearEntityParameters(VideoProcessingEntity entity) {
        entity.setIntervalSeconds(null);
        entity.setMaxFrames(null);
        entity.setOutputFormat(null);
    }

    private VideoProcessingJob.ProcessingParameters mapParametersToDomain(VideoProcessingEntity entity) {
        return VideoProcessingJob.ProcessingParameters.builder()
            .intervalSeconds(getValueOrDefault(entity.getIntervalSeconds(), VideoProcessingConstants.DEFAULT_INTERVAL_SECONDS))
            .maxFrames(getValueOrDefault(entity.getMaxFrames(), VideoProcessingConstants.DEFAULT_MAX_FRAMES))
            .outputFormat(getValueOrDefault(entity.getOutputFormat(), VideoProcessingConstants.DEFAULT_OUTPUT_FORMAT))
            .build();
    }

    private VideoProcessingJob.ProcessingResult mapResultToDomain(VideoProcessingEntity entity) {
        return Optional.ofNullable(entity.getFramesExtracted())
            .map(frames -> VideoProcessingJob.ProcessingResult.builder()
                .framesExtracted(frames)
                .processingTimeMs(getValueOrDefault(entity.getProcessingTimeMs(), VideoProcessingConstants.DEFAULT_PROCESSING_TIME_MS))
                .zipFilename(entity.getZipFilename())
                .downloadUrl(entity.getDownloadUrl())
                .build())
            .orElse(null);
    }

    private VideoProcessingEntity.ProcessingStatus mapStatusToEntity(VideoProcessingJob.ProcessingStatus status) {
        return Optional.ofNullable(status)
            .map(s -> switch (s) {
                case PROCESSING -> VideoProcessingEntity.ProcessingStatus.PROCESSING;
                case COMPLETED -> VideoProcessingEntity.ProcessingStatus.COMPLETED;
                case ERROR -> VideoProcessingEntity.ProcessingStatus.ERROR;
            })
            .orElse(null);
    }

    private VideoProcessingJob.ProcessingStatus mapStatusToDomain(VideoProcessingEntity.ProcessingStatus status) {
        return Optional.ofNullable(status)
            .map(s -> switch (s) {
                case PROCESSING -> VideoProcessingJob.ProcessingStatus.PROCESSING;
                case COMPLETED -> VideoProcessingJob.ProcessingStatus.COMPLETED;
                case ERROR -> VideoProcessingJob.ProcessingStatus.ERROR;
            })
            .orElse(null);
    }

    private <T> T getValueOrDefault(T value, T defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }
}
