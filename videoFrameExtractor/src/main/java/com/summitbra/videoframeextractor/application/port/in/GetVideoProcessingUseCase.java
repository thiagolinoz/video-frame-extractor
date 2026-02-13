package com.summitbra.videoframeextractor.application.port.in;

import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;

import java.util.List;
import java.util.Optional;

public interface GetVideoProcessingUseCase {

    Optional<VideoProcessingJob> getProcessingStatus(String videoId);

    List<VideoProcessingJob> getRecentProcessings();

    List<VideoProcessingJob> getProcessingsByStatus(VideoProcessingJob.ProcessingStatus status);
}
