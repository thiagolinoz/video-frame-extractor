package com.fiap.videoframeextractor.infrastructure.adapter.in.messaging;

import com.fiap.videoframeextractor.domain.model.VideoMessage;
import com.fiap.videoframeextractor.domain.ports.in.FrameExtractionServicePort;
import com.fiap.videoframeextractor.infrastructure.adapter.out.messaging.VideoStatusProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoMessageConsumer {

    private final FrameExtractionServicePort frameExtractionService;
    private final VideoStatusProducer statusProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "received-videos",
        groupId = "${kafka.consumer.group-id:video-frame-extractor-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleReceivedVideo(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("=== RECEBIDA MENSAGEM DO VIDEO-APP ===");
        log.info("Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);
        log.info("Payload: {}", message);

        try {
            VideoMessage videoMessage = objectMapper.readValue(message, VideoMessage.class);

            log.info("Vídeo recebido para processamento:");
            log.info("- ID: {}", videoMessage.getIdVideoSend());
            log.info("- Arquivo: {}", videoMessage.getNmVideo());
            log.info("- Usuário: {}", videoMessage.getNmPersonEmail());
            log.info("- Caminho S3: {}", videoMessage.getNmVideoPathOrigin());

            if (!videoMessage.isValidForProcessing()) {
                log.error("Mensagem inválida para processamento: {}", videoMessage);
                acknowledgment.acknowledge();
                return;
            }

            statusProducer.publishProcessingStatus(videoMessage);

            String zipPath = frameExtractionService.processVideo(videoMessage);

            statusProducer.publishCompletedStatus(videoMessage, zipPath);

            acknowledgment.acknowledge();

            log.info("=== PROCESSAMENTO CONCLUÍDO COM SUCESSO ===");
            log.info("Vídeo ID: {}", videoMessage.getIdVideoSend());

        } catch (Exception e) {
            log.error("=== ERRO NO PROCESSAMENTO ===");
            log.error("Erro ao processar vídeo: {}", e.getMessage(), e);

            try {
                VideoMessage videoMessage = objectMapper.readValue(message, VideoMessage.class);
                statusProducer.publishErrorStatus(videoMessage, e.getMessage());
            } catch (Exception parseError) {
                log.error("Erro ao fazer parse da mensagem para publicar erro: {}", parseError.getMessage());
            }

            acknowledgment.acknowledge();
        }
    }
}
