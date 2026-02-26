package com.fiap.videoframeextractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetadata {
    private String fileName;
    private long size;
    private String contentType;
    private String path;
}
