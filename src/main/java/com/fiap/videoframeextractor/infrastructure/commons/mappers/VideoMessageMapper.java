package com.fiap.videoframeextractor.infrastructure.commons.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.videoframeextractor.domain.model.VideoMessage;
import org.springframework.stereotype.Component;

@Component
public class VideoMessageMapper {

    private final ObjectMapper objectMapper;

    public VideoMessageMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public VideoMessage toDomain(String kafkaMessage) {
        try {
            return objectMapper.readValue(kafkaMessage, VideoMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse video message", e);
        }
    }

    public String toJson(VideoMessage videoMessage) {
        try {
            return objectMapper.writeValueAsString(videoMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize video model", e);
        }
    }
}
