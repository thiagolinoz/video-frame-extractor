package com.fiap.videoframeextractor.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.videoframeextractor.domain.model.VideoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoStatusProducer")
class VideoStatusProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private VideoStatusProducer videoStatusProducer;

    private VideoMessage videoMessage;

    @BeforeEach
    void setUp() {
        videoMessage = VideoMessage.builder()
                .idVideoSend("vid-999")
                .nmVideo("clip.mp4")
                .nmPersonEmail("test@fiap.com")
                .nmPersonName("Test User")
                .build();

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.complete(null);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
    }

    @Nested
    @DisplayName("publishProcessingStatus")
    class PublishProcessingStatusTests {

        @Test
        @DisplayName("deve publicar mensagem com status PROCESSING no Kafka")
        void shouldPublishProcessingStatus() {
            videoStatusProducer.publishProcessingStatus(videoMessage);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(anyString(), eq("vid-999"), payloadCaptor.capture());

            assertThat(payloadCaptor.getValue()).contains("PROCESSING");
            assertThat(payloadCaptor.getValue()).contains("vid-999");
        }
    }

    @Nested
    @DisplayName("publishCompletedStatus")
    class PublishCompletedStatusTests {

        @Test
        @DisplayName("deve publicar mensagem com status COMPLETED e caminho do ZIP")
        void shouldPublishCompletedStatusWithZipPath() {
            String zipPath = "s3://bucket/frames/vid-999.zip";

            videoStatusProducer.publishCompletedStatus(videoMessage, zipPath);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(anyString(), eq("vid-999"), payloadCaptor.capture());

            String payload = payloadCaptor.getValue();
            assertThat(payload).contains("COMPLETED");
            assertThat(payload).contains(zipPath);
        }
    }

    @Nested
    @DisplayName("publishErrorStatus")
    class PublishErrorStatusTests {

        @Test
        @DisplayName("deve publicar mensagem com status PROCESS_ERROR e mensagem de erro")
        void shouldPublishErrorStatusWithErrorMessage() {
            String errorMessage = "FFmpeg crashed";

            videoStatusProducer.publishErrorStatus(videoMessage, errorMessage);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(anyString(), eq("vid-999"), payloadCaptor.capture());

            String payload = payloadCaptor.getValue();
            assertThat(payload).contains("PROCESS_ERROR");
            assertThat(payload).contains(errorMessage);
        }

        @Test
        @DisplayName("deve lançar StatusPublishException quando KafkaTemplate lança exceção")
        void shouldThrowStatusPublishExceptionWhenKafkaFails() {
            when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Kafka broker unavailable"));

            assertThatThrownBy(() -> videoStatusProducer.publishErrorStatus(videoMessage, "some error"))
                    .isInstanceOf(VideoStatusProducer.StatusPublishException.class)
                    .hasMessageContaining("Falha ao publicar status");
        }
    }
}
