package com.summitbra.videoframeextractor.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_processing")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoProcessingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false, unique = true)
    private String videoId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "frames_extracted")
    private Integer framesExtracted;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "zip_filename")
    private String zipFilename;

    @Column(name = "download_url")
    private String downloadUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProcessingStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "interval_seconds")
    private Double intervalSeconds;

    @Column(name = "max_frames")
    private Integer maxFrames;

    @Column(name = "output_format")
    private String outputFormat;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ProcessingStatus {
        PROCESSING, COMPLETED, ERROR
    }
}
