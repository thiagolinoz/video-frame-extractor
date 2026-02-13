package com.summitbra.videoframeextractor.infrastructure.adapter.out.messaging;

import com.summitbra.videoframeextractor.application.port.out.StatusPublisherService;
import com.summitbra.videoframeextractor.domain.model.ProcessStatus;
import com.summitbra.videoframeextractor.domain.model.ProcessStatusMessage;
import com.summitbra.videoframeextractor.domain.model.VideoMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
@ConditionalOnBean(KafkaTemplate.class)
public class KafkaStatusProducerAdapter implements StatusPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.process-status:process-status-videos}")
    private String statusTopic;

    public KafkaStatusProducerAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendProcessingStatus(VideoMessage videoMessage, ProcessStatus status, String message) {
        ProcessStatusMessage statusMessage = new ProcessStatusMessage(
            videoMessage.getVideoId(),
            videoMessage.getUserId(),
            status,
            message,
            LocalDateTime.now(),
            Map.of("originalFileName", videoMessage.getOriginalFileName())
        );
        sendProcessingStatus(statusMessage);
    }

    @Override
    public void sendProcessingStatus(ProcessStatusMessage statusMessage) {
        try {
            kafkaTemplate.send(statusTopic, statusMessage.getVideoId(), statusMessage)
                    .whenComplete((result, failure) -> {
                        if (failure != null) {
                            log.error("Erro ao enviar status para vídeo {}: {}",
                                    statusMessage.getVideoId(), failure.getMessage());
                        } else {
                            log.info("Status {} enviado para vídeo {}",
                                    statusMessage.getStatus(), statusMessage.getVideoId());
                        }
                    });
        } catch (Exception e) {
            log.error("Erro ao publicar status no Kafka: {}", e.getMessage(), e);
        }
    }
}

@Component
@Slf4j
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "false", matchIfMissing = true)
class NoOpStatusPublisherAdapter implements StatusPublisherService {

    @Override
    public void sendProcessingStatus(VideoMessage videoMessage, ProcessStatus status, String message) {
        log.info("Status enviado (NoOp): {} - {} - {}", videoMessage.getVideoId(), status, message);
    }

    @Override
    public void sendProcessingStatus(ProcessStatusMessage statusMessage) {
        log.info("Status enviado (NoOp): {} - {}", statusMessage.getVideoId(), statusMessage.getStatus());
    }
}
