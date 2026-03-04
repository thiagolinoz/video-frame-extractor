package com.fiap.videoframeextractor.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.videoframeextractor.domain.model.VideoMessage;
import com.fiap.videoframeextractor.domain.ports.in.FrameExtractionServicePort;
import com.fiap.videoframeextractor.infrastructure.adapter.out.messaging.VideoStatusProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoMessageConsumer")
class VideoMessageConsumerTest {

    @Mock
    private FrameExtractionServicePort frameExtractionService;

    @Mock
    private VideoStatusProducer statusProducer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private VideoMessageConsumer videoMessageConsumer;

    private String validPayload;
    private VideoMessage validVideoMessage;

    @BeforeEach
    void setUp() {
        validVideoMessage = VideoMessage.builder()
                .idVideoSend("vid-001")
                .nmVideo("sample.mp4")
                .nmVideoPathOrigin("s3://bucket/videos/sample.mp4")
                .nmPersonEmail("user@fiap.com")
                .nmPersonName("User FIAP")
                .build();

        validPayload = """
                {
                  "idVideoSend": "vid-001",
                  "nmVideo": "sample.mp4",
                  "nmVideoPathOrigin": "s3://bucket/videos/sample.mp4",
                  "nmPersonEmail": "user@fiap.com",
                  "nmPersonName": "User FIAP"
                }
                """;
    }

    @Nested
    @DisplayName("handleReceivedVideo - fluxo de sucesso")
    class SuccessFlow {

        @Test
        @DisplayName("deve processar vídeo válido e fazer acknowledge")
        void shouldProcessValidVideoAndAcknowledge() {
            when(frameExtractionService.processVideo(any(VideoMessage.class)))
                    .thenReturn("s3://bucket/frames/vid-001.zip");

            videoMessageConsumer.handleReceivedVideo(
                    validPayload, "received-videos", 0, 1L, acknowledgment);

            verify(statusProducer).publishProcessingStatus(any(VideoMessage.class));
            verify(frameExtractionService).processVideo(any(VideoMessage.class));
            verify(statusProducer).publishCompletedStatus(any(VideoMessage.class), eq("s3://bucket/frames/vid-001.zip"));
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("deve publicar status PROCESSING antes de processar")
        void shouldPublishProcessingStatusBeforeProcessing() {
            when(frameExtractionService.processVideo(any())).thenReturn("zip/path");

            videoMessageConsumer.handleReceivedVideo(
                    validPayload, "received-videos", 0, 10L, acknowledgment);

            var inOrder = inOrder(statusProducer, frameExtractionService);
            inOrder.verify(statusProducer).publishProcessingStatus(any());
            inOrder.verify(frameExtractionService).processVideo(any());
            inOrder.verify(statusProducer).publishCompletedStatus(any(), anyString());
        }
    }

    @Nested
    @DisplayName("handleReceivedVideo - mensagem inválida")
    class InvalidMessageFlow {

        @Test
        @DisplayName("deve ignorar mensagem com campos obrigatórios nulos e fazer acknowledge")
        void shouldIgnoreMessageWithMissingRequiredFields() {
            String incompletePayload = """
                    {
                      "idVideoSend": "vid-002",
                      "nmVideo": null,
                      "nmPersonEmail": null,
                      "nmVideoPathOrigin": null
                    }
                    """;

            videoMessageConsumer.handleReceivedVideo(
                    incompletePayload, "received-videos", 0, 2L, acknowledgment);

            verify(frameExtractionService, never()).processVideo(any());
            verify(statusProducer, never()).publishProcessingStatus(any());
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("deve fazer acknowledge mesmo com JSON inválido")
        void shouldAcknowledgeEvenWithInvalidJson() {
            String badJson = "not-a-json-payload";

            videoMessageConsumer.handleReceivedVideo(
                    badJson, "received-videos", 0, 3L, acknowledgment);

            verify(frameExtractionService, never()).processVideo(any());
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("handleReceivedVideo - fluxo de erro")
    class ErrorFlow {

        @Test
        @DisplayName("deve publicar status PROCESS_ERROR quando processamento lança exceção")
        void shouldPublishErrorStatusWhenProcessingThrows() {
            when(frameExtractionService.processVideo(any()))
                    .thenThrow(new RuntimeException("FFmpeg failure"));

            videoMessageConsumer.handleReceivedVideo(
                    validPayload, "received-videos", 0, 4L, acknowledgment);

            verify(statusProducer).publishErrorStatus(any(VideoMessage.class), contains("FFmpeg failure"));
            verify(statusProducer, never()).publishCompletedStatus(any(), any());
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("deve fazer acknowledge mesmo quando publicação de erro falha")
        void shouldAcknowledgeEvenWhenErrorPublishFails() {
            when(frameExtractionService.processVideo(any()))
                    .thenThrow(new RuntimeException("Processing error"));
            doThrow(new RuntimeException("Kafka down"))
                    .when(statusProducer).publishErrorStatus(any(), any());

            videoMessageConsumer.handleReceivedVideo(
                    validPayload, "received-videos", 0, 5L, acknowledgment);

            verify(acknowledgment).acknowledge();
        }
    }
}
