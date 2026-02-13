package com.summitbra.videoframeextractor.infrastructure.adapter.in.rest.mapper;

import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import com.summitbra.videoframeextractor.infrastructure.adapter.in.rest.dto.VideoProcessingResponseDto;
import org.springframework.stereotype.Component;

@Component
public class VideoProcessingMapper {

    public VideoProcessingResponseDto toResponseDto(VideoProcessingJob job) {
        String status = switch (job.getStatus()) {
            case COMPLETED -> "SUCCESS";
            case ERROR -> "ERROR";
            case PROCESSING -> "PROCESSING";
        };

        return VideoProcessingResponseDto.builder()
            .status(status)
            .message(getStatusMessage(job.getStatus()))
            .videoId(job.getVideoId())
            .originalFilename(job.getOriginalFilename())
            .fileSize(job.getFileSize())
            .zipFileName(job.getResult() != null ? job.getResult().getZipFilename() : null)
            .downloadUrl(job.getResult() != null ? job.getResult().getDownloadUrl() : null)
            .framesExtracted(job.getResult() != null ? job.getResult().getFramesExtracted() : null)
            .processingTimeMs(job.getResult() != null ? job.getResult().getProcessingTimeMs() : null)
            .errorMessage(job.getErrorMessage())
            .createdAt(job.getCreatedAt())
            .updatedAt(job.getUpdatedAt())
            .parameters(job.getParameters() != null ?
                VideoProcessingResponseDto.ProcessingParametersDto.builder()
                    .intervalSeconds(job.getParameters().getIntervalSeconds())
                    .maxFrames(job.getParameters().getMaxFrames())
                    .outputFormat(job.getParameters().getOutputFormat())
                    .build() : null)
            .build();
    }

    private String getStatusMessage(VideoProcessingJob.ProcessingStatus status) {
        return switch (status) {
            case COMPLETED -> "Frames extraídos com sucesso";
            case ERROR -> "Erro ao processar vídeo";
            case PROCESSING -> "Processando vídeo";
        };
    }
}
