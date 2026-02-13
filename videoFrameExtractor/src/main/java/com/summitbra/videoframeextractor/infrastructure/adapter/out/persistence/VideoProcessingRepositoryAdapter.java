package com.summitbra.videoframeextractor.infrastructure.adapter.out.persistence;

import com.summitbra.videoframeextractor.application.port.out.VideoProcessingRepository;
import com.summitbra.videoframeextractor.domain.model.VideoProcessingJob;
import com.summitbra.videoframeextractor.infrastructure.adapter.out.persistence.entity.VideoProcessingEntity;
import com.summitbra.videoframeextractor.infrastructure.adapter.out.persistence.mapper.VideoProcessingPersistenceMapper;
import com.summitbra.videoframeextractor.infrastructure.adapter.out.persistence.repository.JpaVideoProcessingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VideoProcessingRepositoryAdapter implements VideoProcessingRepository {

    private final JpaVideoProcessingRepository jpaRepository;
    private final VideoProcessingPersistenceMapper mapper;

    @Override
    public VideoProcessingJob save(VideoProcessingJob job) {
        Optional<VideoProcessingEntity> existingEntity = jpaRepository.findByVideoId(job.getVideoId());

        VideoProcessingEntity entity;
        if (existingEntity.isPresent()) {
            entity = existingEntity.get();
            mapper.updateEntity(entity, job);
        } else {
            entity = mapper.toEntity(job);
        }

        VideoProcessingEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VideoProcessingJob> findByVideoId(String videoId) {
        return jpaRepository.findByVideoId(videoId)
            .map(mapper::toDomain);
    }

    @Override
    public List<VideoProcessingJob> findByStatusOrderByCreatedAtDesc(VideoProcessingJob.ProcessingStatus status) {
        VideoProcessingEntity.ProcessingStatus entityStatus = mapStatus(status);
        return jpaRepository.findByStatusOrderByCreatedAtDesc(entityStatus)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<VideoProcessingJob> findOldProcessingRecords(LocalDateTime cutoffTime,
                                                           VideoProcessingJob.ProcessingStatus status) {
        VideoProcessingEntity.ProcessingStatus entityStatus = mapStatus(status);
        return jpaRepository.findOldProcessingRecords(cutoffTime, entityStatus)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<VideoProcessingJob> findTop10ByOrderByCreatedAtDesc() {
        return jpaRepository.findTop10ByOrderByCreatedAtDesc()
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void delete(VideoProcessingJob job) {
        jpaRepository.findByVideoId(job.getVideoId())
            .ifPresent(jpaRepository::delete);
    }

    private VideoProcessingEntity.ProcessingStatus mapStatus(VideoProcessingJob.ProcessingStatus status) {
        return switch (status) {
            case PROCESSING -> VideoProcessingEntity.ProcessingStatus.PROCESSING;
            case COMPLETED -> VideoProcessingEntity.ProcessingStatus.COMPLETED;
            case ERROR -> VideoProcessingEntity.ProcessingStatus.ERROR;
        };
    }
}
