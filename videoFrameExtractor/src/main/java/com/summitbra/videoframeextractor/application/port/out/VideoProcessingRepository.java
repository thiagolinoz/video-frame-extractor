package com.summitbra.videoframeextractor.application.port.out;

import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VideoProcessingRepository {

    VideoProcessingJob save(VideoProcessingJob job);

    Optional<VideoProcessingJob> findByVideoId(String videoId);

    List<VideoProcessingJob> findByStatusOrderByCreatedAtDesc(VideoProcessingJob.ProcessingStatus status);

    List<VideoProcessingJob> findOldProcessingRecords(LocalDateTime cutoffTime,
                                                     VideoProcessingJob.ProcessingStatus status);

    List<VideoProcessingJob> findTop10ByOrderByCreatedAtDesc();

    void delete(VideoProcessingJob job);
}
