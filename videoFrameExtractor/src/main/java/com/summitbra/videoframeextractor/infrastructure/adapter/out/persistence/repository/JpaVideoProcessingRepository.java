package com.summitbra.videoframeextractor.infrastructure.adapter.out.persistence.repository;

import com.summitbra.videoframeextractor.infrastructure.adapter.out.persistence.entity.VideoProcessingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaVideoProcessingRepository extends JpaRepository<VideoProcessingEntity, Long> {

    Optional<VideoProcessingEntity> findByVideoId(String videoId);

    List<VideoProcessingEntity> findByStatusOrderByCreatedAtDesc(VideoProcessingEntity.ProcessingStatus status);

    @Query("SELECT v FROM VideoProcessingEntity v WHERE v.createdAt < :cutoffTime AND v.status = :status")
    List<VideoProcessingEntity> findOldProcessingRecords(@Param("cutoffTime") LocalDateTime cutoffTime,
                                                        @Param("status") VideoProcessingEntity.ProcessingStatus status);

    List<VideoProcessingEntity> findTop10ByOrderByCreatedAtDesc();
}
