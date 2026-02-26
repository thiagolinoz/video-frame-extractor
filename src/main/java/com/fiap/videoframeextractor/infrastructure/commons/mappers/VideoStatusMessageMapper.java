package com.fiap.videoframeextractor.infrastructure.commons.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.videoframeextractor.domain.model.VideoStatusMessage;
import org.springframework.stereotype.Component;

@Component
public class VideoStatusMessageMapper {

    private final ObjectMapper objectMapper;

    public VideoStatusMessageMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public String toJson(VideoStatusMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize status message", e);
        }
    }

    public VideoStatusMessage fromJson(String json) {
        try {
            return objectMapper.readValue(json, VideoStatusMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse status message", e);
        }
    }
}
