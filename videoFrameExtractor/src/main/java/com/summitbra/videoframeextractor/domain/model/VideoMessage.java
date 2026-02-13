package com.summitbra.videoframeextractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoMessage {
    private String videoId;
    private String videoPath;
    private String userId;
    private String originalFileName;
    private Map<String, Object> metadata;
}
