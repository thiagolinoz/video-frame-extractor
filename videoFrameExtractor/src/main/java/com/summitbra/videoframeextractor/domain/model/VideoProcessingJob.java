package com.summitbra.videoframeextractor.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class VideoProcessingJob {
    String videoId;
    String originalFilename;
    Long fileSize;
    ProcessingParameters parameters;
    ProcessingStatus status;
    String errorMessage;
    ProcessingResult result;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @Value
    @Builder
    public static class ProcessingParameters {
        double intervalSeconds;
        int maxFrames;
        String outputFormat;
    }

    @Value
    @Builder
    public static class ProcessingResult {
        int framesExtracted;
        long processingTimeMs;
        String zipFilename;
        String downloadUrl;
    }

    public enum ProcessingStatus {
        PROCESSING, COMPLETED, ERROR
    }

    public VideoProcessingJob withStatus(ProcessingStatus status) {
        return this.toBuilder().status(status).updatedAt(LocalDateTime.now()).build();
    }

    public VideoProcessingJob withError(String errorMessage) {
        return this.toBuilder()
            .status(ProcessingStatus.ERROR)
            .errorMessage(errorMessage)
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public VideoProcessingJob withResult(ProcessingResult result) {
        return this.toBuilder()
            .status(ProcessingStatus.COMPLETED)
            .result(result)
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
