package com.summitbra.videoframeextractor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStatusMessage {
    private String videoId;
    private String userId;
    private ProcessStatus status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> details;
}
