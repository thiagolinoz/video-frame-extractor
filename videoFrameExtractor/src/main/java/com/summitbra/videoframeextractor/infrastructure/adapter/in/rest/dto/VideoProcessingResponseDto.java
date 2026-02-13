package com.summitbra.videoframeextractor.infrastructure.adapter.in.rest.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class VideoProcessingResponseDto {
    String status;
    String message;
    String videoId;
    String originalFilename;
    Long fileSize;
    String zipFileName;
    String downloadUrl;
    Integer framesExtracted;
    Long processingTimeMs;
    String errorMessage;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    ProcessingParametersDto parameters;

    @Value
    @Builder
    public static class ProcessingParametersDto {
        double intervalSeconds;
        int maxFrames;
        String outputFormat;
    }

    public static VideoProcessingResponseDto success(String videoId, String zipFileName, String downloadUrl,
                                                    int framesExtracted, long processingTimeMs) {
        return VideoProcessingResponseDto.builder()
            .status("SUCCESS")
            .message("Frames extraídos com sucesso")
            .videoId(videoId)
            .zipFileName(zipFileName)
            .downloadUrl(downloadUrl)
            .framesExtracted(framesExtracted)
            .processingTimeMs(processingTimeMs)
            .build();
    }

    public static VideoProcessingResponseDto error(String errorMessage) {
        return VideoProcessingResponseDto.builder()
            .status("ERROR")
            .message("Erro ao processar vídeo")
            .errorMessage(errorMessage)
            .build();
    }
}
