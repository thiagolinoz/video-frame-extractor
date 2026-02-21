package com.fiap.videoframeextractor.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.videoframeextractor.domain.model.VideoMessage;
import com.fiap.videoframeextractor.domain.model.VideoStatusMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoStatusProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.producer.topic.video-status:process-status-videos}")
    private String videoStatusTopic;

    public void publishProcessingStatus(VideoMessage videoMessage) {
        VideoStatusMessage statusMessage = VideoStatusMessage.processing(
                videoMessage.getIdVideoSend(),
                videoMessage.getNmPersonEmail(),
                videoMessage.getNmVideo(),
                videoMessage.getNmPersonName()
        );

        publishStatus(statusMessage, "PROCESSING");
    }

    public void publishCompletedStatus(VideoMessage videoMessage, String zipPath) {
        VideoStatusMessage statusMessage = VideoStatusMessage.completed(
                videoMessage.getIdVideoSend(),
                videoMessage.getNmPersonEmail(),
                videoMessage.getNmVideo(),
                videoMessage.getNmPersonName(),
                zipPath
        );

        publishStatus(statusMessage, "COMPLETED");
    }

    public void publishErrorStatus(VideoMessage videoMessage, String errorMessage) {
        VideoStatusMessage statusMessage = VideoStatusMessage.error(
                videoMessage.getIdVideoSend(),
                videoMessage.getNmPersonEmail(),
                videoMessage.getNmVideo(),
                videoMessage.getNmPersonName()
        );

        statusMessage.setErrorMessage(errorMessage);

        publishStatus(statusMessage, "ERROR");
    }

    private void publishStatus(VideoStatusMessage statusMessage, String logType) {
        try {
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String messageJson = objectMapper.writeValueAsString(statusMessage);

            log.info("=== PUBLICANDO STATUS {} ===", logType);
            log.info("Topic: {}", videoStatusTopic);
            log.info("VideoId: {}", statusMessage.getIdVideoSend());
            log.info("Status: {}", statusMessage.getCdVideoStatus());
            log.info("Payload: {}", messageJson);

            kafkaTemplate.send(videoStatusTopic, statusMessage.getIdVideoSend(), messageJson)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Erro ao publicar status {}: {}", logType, ex.getMessage(), ex);
                        } else {
                            log.info("Status {} publicado com sucesso: partition={}, offset={}",
                                    logType,
                                    result != null ? result.getRecordMetadata().partition() : "unknown",
                                    result != null ? result.getRecordMetadata().offset() : "unknown");
                        }
                    });

        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar mensagem de status {}: {}", logType, e.getMessage(), e);
            throw new StatusPublishException("Falha ao serializar status: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro ao publicar status {}: {}", logType, e.getMessage(), e);
            throw new StatusPublishException("Falha ao publicar status: " + e.getMessage(), e);
        }
    }

    public static class StatusPublishException extends RuntimeException {
        public StatusPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
