package com.summitbra.videoframeextractor.application.usecase;

import com.summitbra.videoframeextractor.application.port.in.GetVideoProcessingUseCase;
import com.summitbra.videoframeextractor.application.port.out.VideoProcessingRepository;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetVideoProcessingService implements GetVideoProcessingUseCase {

    private final VideoProcessingRepository repository;

    @Override
    public Optional<VideoProcessingJob> getProcessingStatus(String videoId) {
        return repository.findByVideoId(videoId);
    }

    @Override
    public List<VideoProcessingJob> getRecentProcessings() {
        return repository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public List<VideoProcessingJob> getProcessingsByStatus(VideoProcessingJob.ProcessingStatus status) {
        return repository.findByStatusOrderByCreatedAtDesc(status);
    }
}
